export interface IMinetest {

}

export interface IWorld {
    _id?: string;
    owner_id: string;
    owner_name: string,

    created_at: Date;
    updated_at: Date;
    password: string;
    status?: boolean;

    img?: string,
    shared?: boolean,
    title: string,

    selected?: boolean
}

export class Worlds {
    all: IWorld[];
}