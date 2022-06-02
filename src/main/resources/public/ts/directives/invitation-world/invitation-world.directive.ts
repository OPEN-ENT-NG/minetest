import {idiom, ng} from "entcore";
import {IScope} from "angular";
import {RootsConst} from "../../core/constants/roots.const";


interface IViewModel {
    openInvitationLightbox(): void;
    closeInvitationLightbox(): void;

    lightbox: any;

    mail: {
        link: string,
        subject: string,
        body: string,
        invitees: string[]
    };
}

class Controller implements ng.IController, IViewModel {
    lightbox: any;
    mail: {
        link: string,
        subject: string,
        body: string,
        invitees: string[]
    };

    constructor(private $scope: IScope) {
        {
            this.lightbox = {
                invitation: false,
            };
        }
    }

    $onInit() {
    }

    $onDestroy() {
    }

    openInvitationLightbox(): void {
        this.lightbox.invitation = true;
        this.initMail();
    }

    closeInvitationLightbox(): void {
        this.lightbox.invitation = false;
    }

    initMail(): void {
        this.mail.link = ``;
        this.mail.subject = idiom.translate('minetest.invitation.default.subject');
        this.mail.body = idiom.translate('minetest.invitation.default.body');
        this.mail.invitees = [];
    }

}

function directive() {
    return {
        restrict: 'E',
        templateUrl: `${RootsConst.directive}invitation-world/invitation-world.html`,
        scope: {
            onInvitationWorld: '&'
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
export const invitationWorld = ng.directive('invitationWorld', directive)