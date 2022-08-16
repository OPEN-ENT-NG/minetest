import {_, moment, ng, toasts} from "entcore";
import {RootsConst} from "../../core/constants/roots.const";
import {IScope} from "angular";
import {IImportWorld, IWorld} from "../../models";
import {DateUtils} from "../../utils/date.utils";
import {minetestService} from "../../services";
import {safeApply} from "../../utils/safe-apply.utils";

interface IViewModel {
    lightbox: any;

    // props
    world: IWorld | IImportWorld;

    openFollowLightbox(): void;
    closeFollowLightbox(): void;
    showTable(): boolean;
    removeUser(login:string): Promise<void>;
    openInvitation(): void;

}

class Controller implements ng.IController, IViewModel {
    lightbox: any;
    world: IWorld | IImportWorld;

    constructor(private $scope: IScope) {
        {
            this.lightbox = {
                follow: false,
            };
        }
    }

    $onInit() {
    }

    $onDestroy() {
    }

    openFollowLightbox(): void {
        this.lightbox.follow = true;
    }

    closeFollowLightbox(): void {
        this.lightbox.follow = false;
    }

    showTable(): boolean {
        return this.world.whitelist && this.world.whitelist.length > 1;
    }

    openInvitation(): void {
        this.closeFollowLightbox();
        this.$scope.$parent.$eval(this.$scope['vm']['onOpenPopUpInvitation']);
    }

    async removeUser(login:string): Promise<void> {
        this.world.updated_at = DateUtils.format(moment().startOf('minute'),
            DateUtils.FORMAT["DAY/MONTH/YEAR-HOUR-MIN"]);
        this.world.whitelist = _.filter(this.world.whitelist, function (user) {
            return user.login != login;
        });
        minetestService.invite(this.world)
            .then((world:IWorld) => {
                this.world.whitelist = world.whitelist;
                toasts.confirm('minetest.world.invite.modify.confirm');
                safeApply(this.$scope);
            }).catch(() => {
            toasts.warning('minetest.world.invite.modify.error');
        });
    }
}

function directive() {
    return {
        restrict: 'E',
        templateUrl: `${RootsConst.directive}follow-world/follow-world.html`,
        scope: {
            world: '=',
            onOpenPopUpInvitation: '&'
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
export const followWorld = ng.directive('followWorld', directive)