import {model, moment, ng, toasts} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IImportWorld} from "../../models";
import {minetestService} from "../../services";
import {DateUtils} from "../../utils/date.utils";

interface IViewModel {
    openImportLightbox(): void;
    closeImportLightbox(): void;
    importWorld(): Promise<void>;
    resetForm(): void;

    lightbox: any;
    world: IImportWorld;
}

class Controller implements ng.IController, IViewModel {
    lightbox: any;
    world: IImportWorld;

    constructor(private $scope: IScope) {
        {
            this.lightbox = {
                import: false,
            };
        }
    }

    $onInit() {
    }

    $onDestroy() {
    }

    openImportLightbox(): void {
        this.lightbox.import = true;
    }

    closeImportLightbox(): void {
        this.lightbox.import = false;
    }

    resetForm(): void {
        if(this.world) {
            if(this.world.title) {
                this.world.title = "";
            }
            if(this.world.address) {
                this.world.address = "";
            }
            if(this.world.port) {
                this.world.port = "";
            }
            if(this.world.img) {
                this.world.img = "";
            }
        }
    }

    async importWorld(): Promise<void> {
        this.world = {
            owner_id:  model.me.userId,
            owner_name: model.me.username,
            owner_login: model.me.login,
            created_at: DateUtils.format(moment().startOf('minute'), DateUtils.FORMAT["DAY/MONTH/YEAR-HOUR-MIN"]),
            updated_at: DateUtils.format(moment().startOf('minute'), DateUtils.FORMAT["DAY/MONTH/YEAR-HOUR-MIN"]),
            img: this.world.img,
            title: this.world.title,
            address: this.world.address,
            port: this.world.port,
            isExternal: true
        }
        minetestService.import(this.world)
            .then(() => {
                toasts.confirm('minetest.world.import.confirm');
                this.closeImportLightbox();
                this.$scope.$eval(this.$scope['vm']['onImportWorld']());
            }).catch(() => {
            toasts.warning('minetest.world.import.error');
        })
    }
}

function directive() {
    return {
        restrict: 'E',
        templateUrl: `${RootsConst.directive}import-world/import-world.html`,
        scope: {
            onImportWorld: '&'
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
export const importWorld = ng.directive('importWorld', directive)