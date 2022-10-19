/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { TorrentFile } from './TorrentFile';

export type Torrent = {
    files?: Array<TorrentFile>;
    id?: number;
    info?: Array<string>;
    pieceLength?: number;
    sha1?: Array<string>;
    sha256?: Array<string>;
};
