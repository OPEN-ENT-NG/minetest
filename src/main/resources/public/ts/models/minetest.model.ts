export interface IMinetest {

}

export interface IWorld {
    _id?: string;
    owner_id: string;
    owner_name: string;
    owner_login: string;

    created_at: Date;
    updated_at: Date;
    password: string;
    status?: boolean;

    img?: string;
    title: string;

    address: string;

    port?: number;
}

export interface IImportWorld {
    _id?: string;
    owner_id: string;
    owner_name: string;
    owner_login: string;

    created_at: Date;
    updated_at: Date;

    img?: string;
    title: string;
    address: string;
    port: string;

    isExternal: boolean;
}

export class Worlds {
    all: IWorld[];
}