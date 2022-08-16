import {ng} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IImportWorld, IWorld} from "../../models";

interface IViewModel {
    openShareLightbox(): void;
    closeShareLightbox(): void;

    lightbox: any;

    // props
    world: IWorld | IImportWorld;
}

class Controller implements ng.IController, IViewModel {
    lightbox: any;
    world: IWorld | IImportWorld;

    constructor(private $scope: IScope) {
        this.lightbox = {
            share: false,
        };
    }

    $onInit() {
    }

    $onDestroy() {
    }

    openShareLightbox(): void {
        this.lightbox.share = true;
    }

    closeShareLightbox(): void {
        this.lightbox.share = false;
    }
}

function directive() {
    return {
        restrict: 'E',
        templateUrl: `${RootsConst.directive}share-world/share-world.html`,
        scope: {
            world: '=',
            onShareWorld: '&'
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
export const shareWorld = ng.directive('shareWorld', directive);