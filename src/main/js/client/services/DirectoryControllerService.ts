/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { DirectoryDTO } from '../models/DirectoryDTO';
import type { DirectoryInputDTO } from '../models/DirectoryInputDTO';
import type { Pageable } from '../models/Pageable';
import type { PageDirectoryDTO } from '../models/PageDirectoryDTO';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class DirectoryControllerService {

    /**
     * @param pageable
     * @returns PageDirectoryDTO OK
     * @throws ApiError
     */
    public static getDirectories(
        pageable: Pageable,
    ): CancelablePromise<PageDirectoryDTO> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/directories',
            query: {
                'pageable': pageable,
            },
        });
    }

    /**
     * @param requestBody
     * @returns DirectoryDTO Created
     * @throws ApiError
     */
    public static postDirectory(
        requestBody: DirectoryInputDTO,
    ): CancelablePromise<DirectoryDTO> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/directory',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @param id
     * @returns DirectoryDTO OK
     * @throws ApiError
     */
    public static getDirectory(
        id: string,
    ): CancelablePromise<DirectoryDTO> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/directory/{id}',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param id
     * @param requestBody
     * @returns DirectoryDTO OK
     * @throws ApiError
     */
    public static putDirectory(
        id: string,
        requestBody: DirectoryInputDTO,
    ): CancelablePromise<DirectoryDTO> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/api/directory/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }

}