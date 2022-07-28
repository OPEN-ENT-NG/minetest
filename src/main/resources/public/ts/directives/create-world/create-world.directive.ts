import {model, moment, ng, toasts} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IWorld} from "../../models";
import {DateUtils} from "../../utils/date.utils";
import {minetestService} from "../../services";
import {AxiosResponse} from "axios";

declare let window: any;

interface IViewModel {
    openCreateLightbox(): void;
    closeCreateLightbox(): void;
    createWorld(): Promise<void>;
    resetForm(): void;
    formatAddress(): string;

    lightbox: any;
    shuttingDown: boolean;
    world: IWorld;
    newWorld: IWorld;
}

class Controller implements ng.IController, IViewModel {
    lightbox: any;
    shuttingDown: boolean;
    world: IWorld;
    newWorld: IWorld;

    constructor(private $scope: IScope)
    {
        this.lightbox = {
            create: false,
        };
    }

    $onInit() {
    }

    $onDestroy() {
    }

    openCreateLightbox(): void {
        this.lightbox.create = true;
        this.shuttingDown = true;
        let newWorld: IWorld = this.world;
        this.world = Object.assign({}, newWorld);
    }

    closeCreateLightbox(): void {
        this.resetForm();
        this.lightbox.create = false;
    }

    resetForm(): void {
        if (this.world) {
            if (this.world.title) {
                this.world.title = "";
            }
            if (this.world.password) {
                this.world.password = "";
            }
            if (this.world.img) {
                this.world.img = "";
            }
        }
    }

    formatAddress(): string {
        let address = window.minetestServer;
        return address.includes("https://") ? window.minetestServer.replace('https://', '')
            : window.minetestServer.replace('http://', '')
    }

    async createWorld(): Promise<void> {
        this.newWorld = {
            owner_id:  model.me.userId,
            owner_name: model.me.username,
            owner_login: model.me.login,
            created_at: DateUtils.format(moment().startOf('minute'), DateUtils.FORMAT["DAY/MONTH/YEAR-HOUR-MIN"]),
            updated_at: DateUtils.format(moment().startOf('minute'), DateUtils.FORMAT["DAY/MONTH/YEAR-HOUR-MIN"]),
            password: this.world.password,
            status: false,
            img: this.world.img,
            title: this.world.title,
            address: this.formatAddress()
        }
        minetestService.create(this.newWorld)
            .then((world: AxiosResponse) => {
                toasts.confirm('minetest.world.create.confirm');
                if (this.$scope.$parent.$eval(this.$scope['vm']['onCreateWorld'])(world.data))
                    this.$scope.$parent.$eval(this.$scope['vm']['onCreateWorld'])(world.data);
                this.closeCreateLightbox();
            })
            .catch(e => {
                toasts.warning('minetest.world.create.error');
                console.error(e);
                this.closeCreateLightbox();
            })
    }
}

function directive() {
    return {
        restrict: 'E',
        templateUrl: `${RootsConst.directive}create-world/create-world.html`,
        scope: {
            onCreateWorld: '&'
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
export const createWorld = ng.directive('createWorld', directive)