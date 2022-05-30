import {ng, toasts} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IWorld} from "../../models";
import {minetestService} from "../../services";

interface IViewModel {
    openPropertiesLightbox(): void;
    closePropertiesLightbox(): void;
    updateWorld(): Promise<void>;
    updateImportWorld(): Promise<void>;

    lightbox: any;

    // props
    world: IWorld;
    worldForm: IWorld;
}

class Controller implements ng.IController, IViewModel {
    lightbox: any;
    world: IWorld;
    worldForm: IWorld;

    constructor(private $scope: IScope) {
        {
            this.lightbox = {
                properties: false,
            };
        }
    }

    $onInit() {
    }

    $onDestroy() {
    }

    openPropertiesLightbox(): void {
        this.lightbox.properties = true;
        let world: IWorld = this.world;
        this.worldForm = Object.assign({}, world);
    }

    closePropertiesLightbox(): void {
        this.lightbox.properties = false;
    }

    async updateWorld(): Promise<void> {
        minetestService.update(this.worldForm)
            .then(() => {
                toasts.confirm('minetest.world.update.confirm');
                this.closePropertiesLightbox();
                this.$scope.$eval(this.$scope['vm']['onUpdateWorld']());
            }).catch(() => {
            toasts.warning('minetest.world.update.error');
        })
    }

    async updateImportWorld(): Promise<void> {
        minetestService.updateImportWorld(this.worldForm)
            .then(() => {
                toasts.confirm('minetest.world.update.confirm');
                this.closePropertiesLightbox();
                this.$scope.$eval(this.$scope['vm']['onUpdateWorld']);
            }).catch(() => {
                toasts.warning('minetest.world.update.error');
        })
    }
}

function directive() {
    return {
        restrict: 'E',
        templateUrl: `${RootsConst.directive}properties-world/properties-world.html`,
        scope: {
            world: '=',
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
export const propertiesWorld = ng.directive('propertiesWorld', directive)