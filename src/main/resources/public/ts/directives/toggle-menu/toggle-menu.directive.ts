import {ng} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IWorld} from "../../models";

interface IViewModel {

    // props
    world: IWorld;
}

class Controller implements ng.IController, IViewModel {
    world: IWorld;

    constructor(private $scope: IScope) {

    }

    $onInit() {
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