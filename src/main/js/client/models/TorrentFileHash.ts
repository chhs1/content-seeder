/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { TorrentFile } from './TorrentFile';

export type TorrentFileHash = {
    hash: Array<string>;
    id?: number;
    length: number;
    offset: number;
    torrentFile: TorrentFile;
};
