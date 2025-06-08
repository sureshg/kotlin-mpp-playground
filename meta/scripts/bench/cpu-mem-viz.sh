#!/usr/bin/env bash

set -eo pipefail

data_file="top.dat"
action="record"
actions=("record" "vega" "gnuplot")

# Help message
function usage {
  echo "Usage: $(basename "$0") -p <pid> [options]"
  echo "Options:"
  echo "  -p, --pid       PID of the process to monitor (mandatory)"
  echo "  -f, --file      Specify the data file (default: top.dat)"
  echo "  -a, --action    Specify the action to perform. Options are,"
  echo "                  - record   : Record CPU and Memory usage (default)"
  echo "                  - vega     : Generate vega-lite chart"
  echo "                  - gnuplot  : Generate gnuplot chart"
  echo "  -h, --help      Display this help message"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
  case "$1" in
  -f | --file)
    data_file="$2"
    shift 2
    ;;
  -p | --pid)
    pid="$2"
    shift 2
    ;;
  -a | --action)
    action="$2"
    shift 2
    ;;
  -h | --help)
    usage
    exit 0
    ;;
  *)
    echo "Unknown option: $1"
    usage
    exit 1
    ;;
  esac
done

# Validate command line arguments
function validate() {
  if [ -z "$pid" ]; then
    echo "PID is mandatory"
    usage
    exit 1
  fi

  case "${actions[@]}" in
  *"${action}"*)
    # echo "Validation action found!"
    ;;
  *)
    echo "Invalid action: $action"
    usage
    exit 1
    ;;
  esac
}
validate

# Record CPU and Memory usage using top
function record() {
  echo "Recording CPU and Memory usage for PID: $pid"
  rm -f "${data_file}"

  while true; do
    case "$OSTYPE" in
    darwin*)
      top -pid "$pid" -F -l 1 -o cpu -stats 'pid,mem,cpu,command' | tail -n 2 | grep -E '^ *[0-9]+' | awk -v now="$(date +%s.000000000)" '{print now,$1,$2,$3,$4}' >>"${data_file}"
      ;;
    linux*)
      top -p "$pid" -bn 1 | grep -E '^ *[0-9]+' | awk -v now="$(date +%s.%N)" '{print now,$1,$6,$9,$12}' >>"${data_file}"
      ;;
    *)
      echo "Unsupported OS: $OSTYPE"
      exit 1
      ;;
    esac
    # sleep 1
  done
}

function vega_lite() {
  echo "Generating vega-lite visualization..."
  json_data=$(awk '{gsub(/M/,"",$3); print "{\"time\":"$1",\"pid\":"$2",\"memory\":"$3",\"cpu\":"$4",\"process\":\""$5"\"},"}' "$data_file" | sed '$s/,$//' | awk 'BEGIN { print "{\"values\": [" } { print } END { print "]}" }')
  vega_lite_spec=$(
    cat <<-END
{
  "\$schema": "https://vega.github.io/schema/vega-lite/v5.json",
  "description": "CPU and Memory usage",
  "width": 400,
  "height": 300,
  "data": ${json_data} ,
  "encoding": {
    "x": {
      "field": "time",
      "type": "temporal",
      "axis": {
         "title": "Time (seconds)",
         "format": "%H:%M:%S",
         "grid": true,
         "gridDash": [2, 2]
      }
    },
    "color": {
      "field": "process",
      "type": "nominal"
    }
  },
  "layer": [
    {
      "mark": {
        "type": "line",
        "stroke": "#6fbf98",
        "interpolate": "monotone"
      },
      "encoding": {
        "y": {
          "field": "cpu",
          "type": "quantitative",
          "title": "CPU Usage (%)",
          "axis": {
            "titleColor": "#6fbf98",
            "grid": true,
            "gridDash": [2, 2]
          }
        }
      }
    },
    {
      "mark": {
        "stroke": "#ae29bd",
        "type": "line",
        "interpolate": "monotone"
      },
      "encoding": {
        "y": {
          "type": "quantitative",
          "field": "memory",
          "title": "Memory (MB)",
          "axis": {
            "titleColor": "#ae29bd",
            "grid": true,
            "gridDash": [2, 2]
          }
        }
      }
    },
    {
      "mark": "rule",
      "encoding": {
        "opacity": {
          "condition": {
            "value": 0.3,
            "param": "hover",
            "empty": false
          },
          "value": 0
        },
        "tooltip": [
          {
            "field": "cpu",
            "type": "quantitative"
          },
          {
            "field": "memory",
            "type": "quantitative"
          }
        ]
      },
      "params": [
        {
          "name": "hover",
          "select": {
            "type": "point",
            "fields": [
              "time"
            ],
            "nearest": true,
            "on": "mouseover",
            "clear": "mouseout"
          }
        }
      ]
    }
  ],
  "resolve": {
    "scale": {
      "y": "independent"
    }
  }
}
END
  )

  echo "${vega_lite_spec}" >vega-lite-spec.json
  # sudo npm install -g vega-lite vega-cli
  viz_file="vega-lite-${pid}.svg"
  vl2svg -s 1.2 -l debug vega-lite-spec.json "$viz_file"
  echo "Vega-lite visualization file: $viz_file"
}

function gnuplot_viz() {
  echo "Generating gnuplot visualization..."
  gnuplot_commands=$(
    cat <<-END
infile="${data_file}"
outfile="gnuplot-${pid}.svg"
set terminal svg enhanced size 800,600 font "Roboto,12"
set title "PID-${pid} : CPU and Memory usage - " . infile
set output outfile

# Set the x-axis to be the timestamp
set xdata time
set xlabel "Time (seconds)"
set timefmt "%s" # Use Unix timestamp format
set format x "%H:%M:%S" # Display time as hh:mm:ss

# Set the y-axis to be the CPU usage
set ylabel "CPU Usage (%)"
set yrange [0:*]

# Set the y2-axis to be the memory usage
set y2label "Memory Usage (MB)"
set y2range [0:*]
# Set the y2-axis ticks to be on the right side
set y2tics nomirror

set key outside right top
set grid
# set object 1 rectangle from screen 0,0 to screen 1,1 fillcolor rgb "#ADD8E6" fillstyle solid 0.1 behind

# Create a smooth line plot
set style data lines
set style line 1 lw 2 lc rgb "blue"
set style line 2 lw 2 lc rgb "red"
plot infile using 1:4 axes x1y1 with linespoints linestyle 1 title "CPU", \
     infile using 1:(strcol(3)[:-1]+0) axes x1y2 with linespoints linestyle 2 title "Memory"
END
  )

  echo "$gnuplot_commands" | gnuplot
  echo "Gnuplot visualization file: gnuplot-${pid}.svg"
}

case "$action" in
vega*)
  vega_lite
  ;;
gnuplot*)
  gnuplot_viz
  ;;
record)
  record
  ;;
esac
