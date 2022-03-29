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

    img?: File;
    shared?: boolean;
    title: string;

    selected?: boolean;
    address: string;

    port?: number;
    link?: string;
}

export class Worlds {
    all: IWorld[];
}