import {ng} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IWorld} from "../../models";
import {Subject} from "rxjs";
import {ToggleMenuAction} from "./toggle-menu-action.model";
import {TOGGLE_MENU_ACTION} from "./toggle-menu-action.enum";

interface IViewModel {

    // props
    world: IWorld;
    $emitChildren: Subject<ToggleMenuAction>;

    openInvitationLightbox(): void;
}

class Controller implements ng.IController, IViewModel {
    world: IWorld;
    $emitChildren: Subject<ToggleMenuAction> = new Subject<ToggleMenuAction>();

    constructor(private $scope: IScope) {

    }

    $onInit() {
    }

    openInvitationLightbox() {
        // emit
        this.$emitChildren.next({actionComponentType: TOGGLE_MENU_ACTION.INVITATION_WORLD});
    }

    $onDestroy() {
    }
}

function directive() {
    return {
        restrict: 'E',
        templateUrl: `${RootsConst.directive}toggle-menu/toggle-menu.html`,
        scope: {
            world: '=',
            onDeleteWorld: '&',
            onUpdateWorld: '&'
        },
        controllerAs: 'vm',
        bindToController: true,
        controller: ['$scope', Controller],
        link: function (scope: ng.IScope,
                        element: ng.IAugmentedJQuery,
                        attrs: ng.IAttributes,
                        vm: ng.IController) {

        }
    }
}
export const toggleMenu = ng.directive('toggleMenu', directive)