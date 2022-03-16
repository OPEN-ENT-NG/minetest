import {model, moment, ng, toasts} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IWorld, Worlds} from "../../models";
import {DateUtils} from "../../utils/date.utils";
import {minetestService} from "../../services";

interface IViewModel {
    getNbSelectedWorld(): number;
    oneWorldSelected(): boolean;

    // props
    worlds: Worlds;
}

class Controller implements ng.IController, IViewModel {
    worlds: Worlds;

    constructor(private $scope: IScope) {

    }

    $onInit() {
    }

    $onDestroy() {
    }

    getNbSelectedWorld(): number {
        let selectedWorld: number = 0;
        this.worlds.all.forEach((world: IWorld) => {
            if (world.selected) selectedWorld++;
        });
        return selectedWorld;
    }

    oneWorldSelected(): boolean {
        return this.getNbSelectedWorld() == 1;
    }

}

function directive() {
    return {
        restrict: 'E',
        templateUrl: `${RootsConst.directive}toggle-menu/toggle-menu.html`,
        scope: {
            worlds: '='
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