import {ng, _, toasts} from "entcore";
import {User, Users} from "../../models";
import {minetestService} from "../../services";
import {safeApply} from "../../utils/safe-apply.utils";
import {RootsConst} from "../../core/constants/roots.const";


/**
 * @description Displays chips of items list with a search input and dropDown options. If more than
 * 4 items are in the list, only 2 of them will be shown.
 * @param ngModel The list of items to display in chips.
 * @param ngChange Called when the items list changed.
 * @example
 * <recipient-list
 ng-model="<model>"
 ng-change="<function>()">
 </recipient-list>
 */
export const recipientList = ng.directive("recipientList", () => {
    return {
        restrict: "E",
        templateUrl: `${RootsConst.directive}recipient-list/recipient-list.html`,
        scope: {
            ngModel: "=",
            ngChange: "&",
            restriction: "=",
        },

        link: (scope, element) => {
            let firstFocus = true;
            let minWidth = 0;
            scope.focused = false;
            scope.loading = false;
            scope.searchText = "";
            scope.itemsFound = [];
            scope.currentReceiver = "undefined";
            scope.addedFavorites = [];

            element.find("input").on("focus", () => {
                if (firstFocus) firstFocus = false;
                scope.focused = true;
                element.find("div").addClass("focus");
                element.find("form").width(minWidth);
            });

            element.find("input").on("blur", () => {
                scope.focused = false;
                element.find("div").removeClass("focus");
                setTimeout(function() {
                    if (!scope.focused) element.find("form").width(0);
                }, 250);
            });

            element.find("input").on("keydown", function(e) {
                if (
                    e.keyCode === 8 &&
                    scope.searchText &&
                    scope.searchText.length === 0
                ) {
                    // BackSpace
                    const nb = scope.ngModel.length;
                    if (nb > 0) scope.deleteItem(scope.ngModel[nb - 1]);
                }
            });

            //prevent blur when look for more users in dropDown
            element
                .parents()
                .find(".display-more")
                .on("click", () => {
                    if (!firstFocus) {
                        scope.giveFocus();
                    }
                });

            scope.needChipDisplay = () => {
                return (
                    !scope.focused &&
                    typeof scope.ngModel !== "undefined" &&
                    scope.ngModel.length > 3
                );
            };

            scope.update = async (force?: boolean) => {
                if (force) {
                    await scope.doSearch();
                    safeApply(scope);
                } else {
                    if (
                        (scope.restriction && scope.searchText.length < 3) ||
                        scope.searchText.length < 1
                    ) {
                        scope.itemsFound.splice(0, scope.itemsFound.length);
                    } else {
                        await scope.doSearch();
                        safeApply(scope);
                    }
                }
            };

            scope.giveFocus = () => {
                if (!scope.focus) element.find("input").focus();
            };

            scope.unfoldChip = () => {
                if (!firstFocus && scope.needChipDisplay()) {
                    scope.giveFocus();
                }
            };


            scope.addOneItem = (item) => {
                for (let i = 0, l = scope.ngModel.length; i < l; i++) {
                    if (scope.ngModel[i].id === item.id) {
                        return false;
                    }
                }
                scope.ngModel.push(item);
                return true;
            };

            scope.addItem = async (user?:User) => {
                scope.focused = true;
                element.find('input').focus();
                if (!scope.ngModel) {
                    scope.ngModel = [];
                }
                if(user && _.findWhere(scope.ngModel, {id: user.id}) == undefined ){
                    scope.ngModel.push(user);
                } else if (scope.currentReceiver.type === 'sharebookmark') {
                    scope.loading = true;
                    minetestService.getSharebookmark(scope.currentReceiver.id.toString())
                        .then((response) => {
                            response.data.groups.forEach(item => {
                                scope.addOneItem(item);
                            });
                            response.data.users.forEach(item => {
                                scope.addOneItem(item);
                            });
                        }).catch(() => {
                        toasts.warning('minetest.world.invite.error');
                    });
                    scope.addedFavorites.push(scope.currentReceiver);
                    scope.loading = false;
                } else {
                    scope.ngModel.push(scope.currentReceiver);
                }
                setTimeout(function(){
                    scope.itemsFound.splice(scope.itemsFound.indexOf(scope.currentReceiver), 1);
                    safeApply(scope);
                }, 0);
                safeApply(scope);
                scope.$eval(scope.ngChange);
            };

            scope.deleteItem = item => {
                scope.ngModel = _.reject(scope.ngModel, function(i) {
                    return i === item;
                });
                safeApply(scope);
                scope.$eval(scope.ngChange);
                if (scope.itemsFound.length > 0)
                    scope.doSearch();
            };

            scope.clearSearch = () => {
                scope.itemsFound = [];
                scope.searchText = "";
                scope.addedFavorites = [];
                safeApply(scope);
            };

            scope.doSearch = async () => {
                let include = [];
                const exclude = scope.ngModel || [];
                await new Users().findUser(scope.searchText, include, exclude)
                    .then((users) => {
                        Object.assign(scope.itemsFound, users, { length: users.length });
                        scope.itemsFound = _.reject(scope.itemsFound, function (element) {
                            return _.findWhere(scope.addedFavorites, { id: element.id });
                        });
                    }).catch(() => {
                        toasts.warning('minetest.world.invite.error');
                    });
            };

            // Focus when items list changes
            scope.$watchCollection("ngModel", function() {
                if (!firstFocus) {
                    scope.giveFocus();
                }
            });

            // Make the input width be the label help infos width
            setTimeout(function() {
                minWidth = element.find("form label").width();
            }, 0);
        }
    };
});