/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { Pageable } from '../models/Pageable';
import type { PageTorrent } from '../models/PageTorrent';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class MetadataControllerService {

    /**
     * @param requestBody
     * @returns any Created
     * @throws ApiError
     */
    public static uploadMetadata(
        requestBody: Array<string>,
    ): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/metadata',
            body: requestBody,
            mediaType: 'application/x-bittorrent',
        });
    }

    /**
     * @param piecesRootHex
     * @param pageable
     * @returns PageTorrent OK
     * @throws ApiError
     */
    public static searchPiecesRoot(
        piecesRootHex: string,
        pageable: Pageable,
    ): CancelablePromise<PageTorrent> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/metadata/search/{piecesRootHex}',
            path: {
                'piecesRootHex': piecesRootHex,
            },
            query: {
                'pageable': pageable,
            },
        });
    }

    /**
     * @param infoHashHex
     * @returns string OK
     * @throws ApiError
     */
    public static getV1Torrent(
        infoHashHex: string,
    ): CancelablePromise<Array<string>> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/metadata/v1/{infoHashHex}',
            path: {
                'infoHashHex': infoHashHex,
            },
        });
    }

    /**
     * @param infoHashHex
     * @returns string OK
     * @throws ApiError
     */
    public static getV2Torrent(
        infoHashHex: string,
    ): CancelablePromise<Array<string>> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/metadata/v2/{infoHashHex}',
            path: {
                'infoHashHex': infoHashHex,
            },
        });
    }

}