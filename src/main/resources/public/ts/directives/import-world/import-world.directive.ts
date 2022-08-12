import {idiom, model, moment, ng, toasts} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IImportWorld} from "../../models";
import {minetestService} from "../../services";
import {DateUtils} from "../../utils/date.utils";
import {AxiosResponse} from "axios";

interface IViewModel {
    openImportLightbox(): void;
    closeImportLightbox(): void;
    importWorld(): Promise<void>;
    resetForm(): void;

    lightbox: any;
    world: IImportWorld;
    newImportWorld: IImportWorld;
    isPortValid(): boolean;
}

class Controller implements ng.IController, IViewModel {
    lightbox: any;
    world: IImportWorld;
    newImportWorld: IImportWorld;

    constructor(private $scope: IScope) {
        {
            this.lightbox = {
                import: false,
                portAlert: idiom.translate('minetest.world.port.valid')
            };
        }
    }

    $onInit() {
    }

    $onDestroy() {
    }

    openImportLightbox(): void {
        this.lightbox.import = true;
        let newImportWorld: IImportWorld = this.world;
        this.world = Object.assign({}, newImportWorld);
    }

    closeImportLightbox(): void {
        this.lightbox.import = false;
        this.resetForm();
    }

    resetForm(): void {
        if (this.world) {
            if (this.world.title) {
                this.world.title = "";
            }
            if (this.world.address) {
                this.world.address = "";
            }
            if (this.world.port) {
                this.world.port = undefined;
            }
            if (this.world.img) {
                this.world.img = "";
            }
        }
    }

    isPortValid(): boolean {
        if(this.world && this.world.port) {
            let port = (this.world.port).toString();
            let portValid = new RegExp(/\d{1,5}/);
            return portValid.test(port);
        } else {
            return false;
        }
    }

    async importWorld(): Promise<void> {
        this.newImportWorld = {
            myRights: this.world.myRights,
            owner: {displayName: model.me.username, userId: model.me.userId},
            shared: this.world.shared,
            owner_id:  model.me.userId,
            owner_name: model.me.username,
            owner_login: model.me.login,
            created_at: DateUtils.format(moment().startOf('minute'), DateUtils.FORMAT["DAY/MONTH/YEAR-HOUR-MIN"]),
            updated_at: DateUtils.format(moment().startOf('minute'), DateUtils.FORMAT["DAY/MONTH/YEAR-HOUR-MIN"]),
            img: this.world.img,
            title: this.world.title,
            address: this.world.address,
            port: parseInt(String(this.world.port)),
            isExternal: true
        }
        minetestService.import(this.newImportWorld)
            .then((world: AxiosResponse) => {
                toasts.confirm('minetest.world.import.confirm');
                if (this.$scope.$parent.$eval(this.$scope['vm']['onImportWorld'])(world.data))
                    this.$scope.$parent.$eval(this.$scope['vm']['onImportWorld'])(world.data);
                this.closeImportLightbox();
            }).catch(() => {
            toasts.warning('minetest.world.import.error');
            this.closeImportLightbox();
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