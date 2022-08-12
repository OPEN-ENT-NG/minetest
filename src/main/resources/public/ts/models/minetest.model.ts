import {Shareable} from "entcore";

export interface Whitelist {
    id: string,
    login?: string,
    displayName?: string,
    lastName?: string,
    firstName?: string,
    whitelist?: boolean,

    groupDisplayName?: string,
    isGroup?: boolean,
    name?: string,
    profile?: string,
    structureName?: string
}

export class IWorld implements Shareable {
    _id?: string | number;
    owner_id: string;
    owner_name: string;
    owner_login: string;

    shared: any;
    owner: { userId: string; displayName: string };
    myRights: any;

    created_at: string;
    updated_at: string;

    password: string;
    status?: boolean;
    shuttingDown: boolean;

    img?: string;
    title: string;
    address: string;
    port?: number;

    whitelist?: Whitelist[];
    subject?: string;
}

export class IImportWorld implements Shareable {
    _id?: string | number;
    owner_id: string;
    owner_name: string;
    owner_login: string;

    shared: any;
    owner: { userId: string; displayName: string };
    myRights: any;

    created_at: string;
    updated_at: string;

    img?: string;
    title: string;
    address: string;
    port: number;

    isExternal: boolean;

    whitelist?: Whitelist[];
    subject?: string;
}

export class Worlds {
    all: IWorld[];
}