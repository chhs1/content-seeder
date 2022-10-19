/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { DirectoryDTO } from './DirectoryDTO';
import type { Pageable } from './Pageable';
import type { Sort } from './Sort';

export type PageDirectoryDTO = {
    content?: Array<DirectoryDTO>;
    empty?: boolean;
    first?: boolean;
    last?: boolean;
    number?: number;
    numberOfElements?: number;
    pageable?: Pageable;
    size?: number;
    sort?: Sort;
    totalElements?: number;
    totalPages?: number;
};
