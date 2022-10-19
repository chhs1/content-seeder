# Introduction

This project is a proof of concept BitTorrent client designed to seed torrents where the files have been relocated since
their initial download. It scans a directory for files and torrent metadata, and attempts to identify which files came
from which torrents. Upon identification, it will seed those torrents.

# How to use (as currently configured)

Copy any .torrent and content files into the `config/content/` directory. Run the application via either an IDEA
(IntelliJ recommended), or with `./mvnw spring-boot:run`

# How it works

Upon startup, this program will scan the configured directories (see `src/main/application.yml`) for metadata and
content files. Each of the files is checked against all known metadata for matches. When a match is found, the torrent
is added to the libtorrent session in seeding mode. If a torrent is added and there are files missing, they are ignored.

You can view all torrents in the session via the Web UI accessible at http://localhost:8080

# Problems

The current blocker for this program is that it does not scale to thousands of torrents, something that is a necessity
for scanning and seeding large content libraries. Upon adding a lot of torrents, the program will consistently crash
with no error message.