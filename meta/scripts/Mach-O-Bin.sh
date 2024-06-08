#!/usr/bin/env bash

set -euo pipefail

C_CODE=$(
  cat <<EOF
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/ioctl.h>

int main() {
	int tty_fd = open("/dev/tty", O_EVTONLY | O_NONBLOCK);
	if (tty_fd == -1) {
		fprintf(stderr, "Opening $(/dev/tty) failed (%d): %s\n", errno, strerror(errno));
		return 1;
	}

	struct winsize ws;
	int result = ioctl(tty_fd, TIOCGWINSZ, &ws);
	close(tty_fd);

	if (result == -1) {
		fprintf(stderr, "Getting the size failed (%d): %s\n", errno, strerror(errno));
		return 1;
	}

	fprintf(stdout, "%d\n%d\n", ws.ws_col, ws.ws_row);
	return 0;
}
EOF
)

# Create a temporary file to store the C code
TMP_FILE=$(mktemp temp.XXXXXXXXXX)
TERM_SIZE_C="$TMP_FILE.c"
TERM_SIZE_BIN="term-size"

mv "$TMP_FILE" "$TERM_SIZE_C"
echo "$C_CODE" >"$TERM_SIZE_C"
rm -f "$TERM_SIZE_BIN"

#  Create Mach-O universal binary with 2 architectures: x86_64 & arm64
clang -Ofast -Wall -Wextra -pedantic -std=c99 -arch arm64 -arch x86_64 -o "$TERM_SIZE_BIN" "$TERM_SIZE_C"

# Clean up the temporary file
rm "$TERM_SIZE_C"
