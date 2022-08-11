export interface IWorld {
    _id?: string;
    owner_id: string;
    owner_name: string;
    owner_login: string;

    created_at: string;
    updated_at: string;

    password: string;
    status?: boolean;
    shuttingDown: boolean;

    img?: string;
    title: string;
    address: string;
    port?: number;

    whitelist?: string[];
    subject?: string;
}

export interface IImportWorld {
    _id?: string;
    owner_id: string;
    owner_name: string;
    owner_login: string;

    created_at: string;
    updated_at: string;

    img?: string;
    title: string;
    address: string;
    port: number;

    isExternal: boolean;

    whitelist?: string[];
    subject?: string;
}

export class Worlds {
    all: IWorld[];
}