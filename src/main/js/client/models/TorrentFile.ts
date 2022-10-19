/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { Torrent } from './Torrent';
import type { TorrentFileHash } from './TorrentFileHash';

export type TorrentFile = {
    fileIndex?: number;
    hashes?: Array<TorrentFileHash>;
    id?: number;
    length?: number;
    name?: string;
    piecesRoot?: Array<string>;
    torrent?: Torrent;
};
