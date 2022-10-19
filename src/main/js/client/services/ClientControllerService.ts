/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { TorrentDTO } from '../models/TorrentDTO';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class ClientControllerService {

    /**
     * @returns TorrentDTO OK
     * @throws ApiError
     */
    public static getTorrents(): CancelablePromise<Array<TorrentDTO>> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/client/torrents',
        });
    }

}