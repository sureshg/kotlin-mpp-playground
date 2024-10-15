jextract \
        --output jextract/src/main/java \
        --target-package "${PACKAGE_NAME}" \
        --include-struct termios \
        --include-function ioctl \
        --include-function tcgetattr \
        --include-function tcsetattr \
        --include-constant EBADF \
        --include-constant EFAULT \
        --include-constant EINVAL \
        --include-constant ENOTTY \
        --include-constant VINTR \
        --include-constant VQUIT \
        --include-constant VERASE \
        --include-constant VEOF \
        --include-constant VEOL \
        --include-constant VEOL2 \
        --include-constant VSTART \
        --include-constant VSTOP \
        --include-constant VSUSP \
        --include-constant VWERASE \
        --include-constant VREPRINT \
        --include-constant VLNEXT \
        --include-constant VDISCARD \
        --include-constant IGNPAR \
        --include-constant PARMRK \
        --include-constant INPCK \
        --include-constant ISTRIP \
        --include-constant INLCR \
        --include-constant IGNCR \
        --include-constant ICRNL \
        --include-constant IXON \
        --include-constant IXANY \
        --include-constant IXOFF \
        --include-constant IMAXBEL \
        --include-constant OPOST \
        --include-constant ONLCR \
        --include-constant OCRNL \
        --include-constant ONOCR \
        --include-constant ONLRET \
        --include-constant CS7 \
        --include-constant CS8 \
        --include-constant PARENB \
        --include-constant PARODD \
        --include-constant ISIG \
        --include-constant ICANON \
        --include-constant ECHO \
        --include-constant ECHOE \
        --include-constant ECHOK \
        --include-constant ECHONL \
        --include-constant NOFLSH \
        --include-constant TOSTOP \
        --include-constant IEXTEN \
        --include-constant ECHOCTL \
        --include-constant ECHOKE \
        --include-constant PENDIN \
        --include-constant TCGETS \
        --include-constant TCSETS \
        --include-constant IGNBRK \
        --include-constant BRKINT \
        --include-constant PARMRK \
        --include-constant ISTRIP \
        --include-constant INLCR \
        --include-constant IGNCR \
        --include-constant ICRNL \
        --include-constant IXON \
        --include-constant OPOST \
      	--include-constant ECHO \
      	--include-constant ECHONL \
      	--include-constant ICANON \
      	--include-constant ISIG \
      	--include-constant IEXTEN \
      	--include-constant CSIZE \
       	--include-constant PARENB \
      	--include-constant CS8 \
      	--include-constant VMIN \
      	--include-constant VTIME \
      	--include-constant TCGETS \
      	--include-constant TCSETS \
        jextract/gen/c/ioctl.h