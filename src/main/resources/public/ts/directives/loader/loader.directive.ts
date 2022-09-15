import {ng} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";

interface IViewModel {

    // props
    minHeight: string;

}

class Controller implements ng.IController, IViewModel {
    minHeight: string;

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
        templateUrl: `${RootsConst.directive}loader/loader.html`,
        scope: {
            minHeight: '=',
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
export const loader = ng.directive('loader', directive)