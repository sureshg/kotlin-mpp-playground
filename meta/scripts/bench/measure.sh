#!/bin/bash

CMD="$@"
URL="http://localhost:8080/info"

# Record start time
START_TIME=$(date +%s.%N)

# Start app and get PID
$CMD &
PID=$!

# Wait for first successful HTTP 200 response
until curl -sf "$URL" -w "%{http_code}" -o /dev/null | grep -q "200"; do sleep 0.001; done

# oha -n 1000 -c 10 "$URL"

# Calculate wall time
END_TIME=$(date +%s.%N)
WALL_TIME=$(echo "$END_TIME - $START_TIME" | bc)

# Wall Time:    |------ 1.5s ------|  (Real world elapsed time)
# Core 1:       |██████████████████|  (100% busy = 1.5 seconds of work)
# Core 2:       |████████████████  |  ( 70% busy = 1.1 seconds of work)
# Cores 3-10:   |                  |  (  0% busy = idle)
# ─────────────────────────────────────────────────────────────────────
# Summary:
# • Total CPU Time = 1.5 + 1.1 = 2.6 seconds ✓
# • Current CPU% = (100% + 70%) = 170% ≈ 169% ✓
# • Wall Time = 1.5 seconds (time that actually passed)
#
# Key Relationships:
# • Multi-threaded: CPU Time > Wall Time (more work than time passed)
# • CPU% Formula: (CPU Time ÷ Wall Time) × 100%
# • Example: (2.6 ÷ 1.5) × 100% = 173% ≈ 169%
# ======================================================================

ps -o rss,vsz,%cpu,cputime,time -p $PID | tail -1 | awk -v wt="$WALL_TIME" '{print "Memory: " $1 " KB, CPU%: " $3 "%, CPUTime: " $4 ", WallTime: " wt "s"}'

# Cleanup
kill -9 $PID 2>/dev/null