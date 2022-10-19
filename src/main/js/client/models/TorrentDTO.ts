/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

export type TorrentDTO = {
    downloadRate?: number;
    name?: string;
    peers?: number;
    progress?: number;
    seeds?: number;
    size?: number;
    status?: TorrentDTO.status;
    uploadRate?: number;
};

export namespace TorrentDTO {

    export enum status {
        CHECKING_FILES = 'CHECKING_FILES',
        DOWNLOADING_METADATA = 'DOWNLOADING_METADATA',
        DOWNLOADING = 'DOWNLOADING',
        FINISHED = 'FINISHED',
        SEEDING = 'SEEDING',
        CHECKING_RESUME_DATA = 'CHECKING_RESUME_DATA',
        UNKNOWN = 'UNKNOWN',
    }


}
