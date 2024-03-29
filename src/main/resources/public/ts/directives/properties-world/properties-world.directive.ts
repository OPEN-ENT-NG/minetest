import {idiom, ng, toasts} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IWorld} from "../../models";
import {minetestService} from "../../services";
import {safeApply} from "../../utils/safe-apply.utils";

interface IViewModel {
    openPropertiesLightbox(): void;
    closePropertiesLightbox(): void;
    updateWorld(): Promise<void>;
    resetPassword(): void;
    updateImportWorld(): Promise<void>;
    isPortValid(): boolean;

    showInputPassword: boolean;
    lightbox: any;

    // props
    world: IWorld;
    worldForm: IWorld;
}

class Controller implements ng.IController, IViewModel {
    lightbox: any;
    world: IWorld;
    worldForm: IWorld;
    showInputPassword: boolean;

    constructor(private $scope: IScope) {
        this.lightbox = {
            properties: false,
            portAlert: idiom.translate('minetest.world.port.valid')
        };
    }

    $onInit() {
    }

    $onDestroy() {
    }

    openPropertiesLightbox(): void {
        this.lightbox.properties = true;
        this.showInputPassword = false;
        let world: IWorld = this.world;
        this.worldForm = Object.assign({}, world);
        if (this.worldForm.shuttingDown == undefined) {
            this.worldForm.shuttingDown = true;
        }
    }

    closePropertiesLightbox(): void {
        this.lightbox.properties = false;
        safeApply(this.$scope);
    }

    async updateWorld(): Promise<void> {
        minetestService.update(this.worldForm)
            .then(() => {
                toasts.confirm('minetest.world.update.confirm');
                this.closePropertiesLightbox();
                this.$scope.$parent.$eval(this.$scope['vm']['onUpdateWorld'](this.worldForm));
                this.world = {...this.worldForm};
            }).catch(() => {
            toasts.warning('minetest.world.update.error');
            this.closePropertiesLightbox();
        })
    }

    resetPassword(): void {
        this.worldForm.password = "";
        this.showInputPassword = !this.showInputPassword;
    }

    isPortValid(): boolean {
        if(this.worldForm && this.worldForm.port) {
            let port = (this.worldForm.port).toString();
            let portValid = new RegExp(/\d{1,5}/);
            return portValid.test(port);
        } else {
            return false;
        }
    }


    async updateImportWorld(): Promise<void> {
        this.worldForm.port = parseInt(String(this.worldForm.port));
        minetestService.updateImportWorld(this.worldForm)
            .then(() => {
                toasts.confirm('minetest.world.update.confirm');
                this.closePropertiesLightbox();
                this.$scope.$parent.$eval(this.$scope['vm']['onUpdateWorld'](this.worldForm));
            }).catch(() => {
            toasts.warning('minetest.world.update.error');
            this.closePropertiesLightbox();
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
export const propertiesWorld = ng.directive('propertiesWorld', directive);