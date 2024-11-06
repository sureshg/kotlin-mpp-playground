

Create `/etc/systemd/system/docker-compose@.service`
Copy `docker-compose.yml` to `/app/myservice/docker-compose.yml`

```bash
$ systemctl start docker-compose@myservice
```

Copy `docker-cleanup.timer` to `/etc/systemd/system/docker-cleanup.timer`
Copy `docker-cleanup.service` to `/etc/systemd/system/docker-cleanup.service`

```bash
$ systemctl enable docker-cleanup.timer
```

Just add the following line to the /etc/docker/daemon.json:

{
...
"log-driver": "journald",
...
}
And restart your docker service.