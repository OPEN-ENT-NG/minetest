import {ng, toasts} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IWorld, Worlds} from "../../models";
import {minetestService} from "../../services";
import {AxiosResponse} from "axios";

interface IViewModel {
    openDeleteLightbox(): void;
    closeDeleteLightbox(): void;
    deleteWorld(): Promise<void>;
    lightbox: any;

    // props
    worlds: Worlds;
}

class Controller implements ng.IController, IViewModel {
    lightbox: any;
    worlds: Worlds;

    constructor(private $scope: IScope) {
        this.lightbox = {
            delete: false,
        };
    }

    $onInit() {
    }

    $onDestroy() {
    }

    openDeleteLightbox(): void {
        this.lightbox.delete = true;
    }

    closeDeleteLightbox(): void {
        this.lightbox.delete = false;
    }

    async deleteWorld(): Promise<void> {
        this.worlds.all.forEach((world: IWorld) => {
            let response: Promise<AxiosResponse> = (world['isExternal']) ? minetestService.deleteImportWorld(world) : minetestService.delete(world);
            response
                .then(() => {
                    toasts.confirm('minetest.world.delete.confirm');
                    this.closeDeleteLightbox();
                    this.$scope.$parent.$eval(this.$scope['vm']['onDeleteWorld'])(this.worlds);
                })
                .catch(e => {
                    toasts.warning('minetest.world.delete.error');
                    console.error(e);
                    this.closeDeleteLightbox();
                })
        });
    }
}

function directive() {
    return {
        restrict: 'E',
        templateUrl: `${RootsConst.directive}delete-world/delete-world.html`,
        scope: {
            worlds: '=',
            onDeleteWorld: '&'
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
export const deleteWorld = ng.directive('deleteWorld', directive);