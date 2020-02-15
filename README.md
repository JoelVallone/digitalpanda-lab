# digitalpanda-infrastructure
Infrastructure scripts and configuration

#Run docker compose
1. build: `docker-compose -f ./docker/compose/compose.yml build`
2. run: `docker-compose -f ./docker/compose/compose.yml up`

#create nd auto-mount a new partition:
1. list partition tables : `fdisk -l`
2. manage/create a partition on disk sdx: `fdisk /dev/sdx`
3. format a partition on partition sdx1 : `mkfs -t ext4 /dev/sdx1`
4. create and mount new partition: `mkdir /storage && mount /dev/sdx1 /storage`
5. list uuids of each partitions: `ls -l /dev/disk/by-uuid`
6. automount at startup: add line `UUID={partition-uuid-of-sdx1} /storage ext4 errors=remount-ro 1 2`