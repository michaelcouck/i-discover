#!/bin/sh

if [ $# -lt 2 ]; then 
    echo "No destination defined. Usage: $0 source destination" >&2
    exit 1
elif [ $# -gt 2 ]; then
    echo "Too many arguments. Usage: $0 source destination" >&2
    exit 1
fi

START=$(date +%s)
# --delete // option to delete all target files that don't exist 
# --rsh='ssh -p22' // option to transfer over ssh
rsync -avzAXogchP --stats --verbose --progress --exclude /mnt --exclude /sys --exclude /proc --exclude /tmp $1 $2
FINISH=$(date +%s)
echo "total time: $(( ($FINISH-$START) / 60 )) minutes, $(( ($FINISH-$START) % 60 )) seconds" | tee $1/"Backup from $(date '+%A, %d %B %Y, %T')"



