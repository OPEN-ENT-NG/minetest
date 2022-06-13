import {idiom as lang, _, toasts} from "entcore";

import { Mix, Selectable, Eventer } from "entcore-toolkit";
import {minetestService} from "../services";

export class User implements Selectable {
    displayName: string;
    name: string;
    profile: string;
    id: string;
    selected: boolean;
    isGroup: boolean;
    email: string;
    result: boolean;

    constructor(id?: string, displayName?: string) {
        this.displayName = displayName;
        this.id = id;
    }

    toString() {
        return (
            (this.displayName || "") +
            (this.name || "") +
            (this.profile ? " (" + lang.translate(this.profile) + ")" : "")
        );
    }

}

export class Users {
    eventer = new Eventer();
    searchCachedMap = {};

    async sync(search: string): Promise<User[]> {
        let newArr = [];
        let bookmarks = [];
        await minetestService.getSharebookmarks()
            .then(async (sharebooks) => {
                bookmarks = _.map(sharebooks.data, function(bookmark) {
                    bookmark.type = 'sharebookmark';
                    return bookmark;
                });
                newArr = Mix.castArrayAs(User, bookmarks);
                search = search.split(' ').join('');
                await minetestService.getVisibleUsers(search)
                    .then((users) => {
                        users.data.groups.forEach(group => {
                            group.isGroup = true;
                            newArr.push(Mix.castAs(User, group));
                        });
                        newArr = newArr.concat(Mix.castArrayAs(User, users.data.users));
                        return newArr;
                    })
                    .catch((err) => {
                        toasts.warning('minetest.world.invite.error');
                        throw err;
                    });
            }).catch((err) => {
            toasts.warning('minetest.world.invite.error');
            throw err;
        });
        return newArr;
    }

    async findUser(search, include, exclude): Promise<User[]> {
        const startText = search.substr(0, 10);
        let result = [];
        if (!this.searchCachedMap[startText]) {
            this.searchCachedMap[startText] = [];
            await this.sync(startText)
                .then((res) => {
                    this.searchCachedMap[startText] = res;
                    result = this.reformatResults(search, startText, include, exclude);
                }).catch(() => {
                toasts.warning('minetest.world.invite.error');
            });
        } else {
            result = this.reformatResults(search, startText, include, exclude);
        }
        return result;
    }

    private reformatResults(search, startText: string, include, exclude) {
        const searchTerm = lang.removeAccents(search).toLowerCase();
        const found = _.filter(
            this.searchCachedMap[startText]
                .filter(function (user) {
                    const includeUser = _.findWhere(include, {id: user.id});
                    if (includeUser !== undefined)
                        includeUser.profile = user.profile;
                    return includeUser === undefined;
                })
                .concat(include), function (user) {
                let testDisplayName = "",
                    testNameReversed = "";
                if (user.displayName) {
                    testDisplayName = lang
                        .removeAccents(user.displayName)
                        .toLowerCase();
                    testNameReversed = lang
                        .removeAccents(
                            user.displayName.split(" ")[1] +
                            " " +
                            user.displayName.split(" ")[0]
                        )
                        .toLowerCase();
                }
                let testName = "";
                if (user.name) {
                    testName = lang.removeAccents(user.name).toLowerCase();
                }

                return (
                    testDisplayName.indexOf(searchTerm) !== -1 ||
                    testNameReversed.indexOf(searchTerm) !== -1 ||
                    testName.indexOf(searchTerm) !== -1
                );
            }
        );
        return _.reject(found, function (element) {
            return _.findWhere(exclude, {id: element.id});
        });
    }
}
