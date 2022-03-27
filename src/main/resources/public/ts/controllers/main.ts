import {ng, template, model} from 'entcore';

import {IScope} from "angular";

declare let window: any;

interface ViewModel {
	user_id: string;
	user_name: string;
	user_login: string;
	// getMomentFromDate: any;
}

/**
	Wrapper controller
	------------------
	Main controller.
**/

class Controller implements ng.IController, ViewModel {
	user_id: string;
	user_name: string;
	user_login: string;
	// getMomentFromDate: any;

	constructor(private $scope: IScope,
				private $route: any) {
		this.$scope['vm'] = this;

		this.$route({
			defaultView: () => {
				template.open('main', `main`);
			}
		});
	}

	$onInit() {
		this.user_id = model.me.userId;
 		this.user_name = model.me.username;
		this.user_login = model.me.login;
	}

	$onDestroy() {
	}
}

export const mainController = ng.controller('MainController', ['$scope', 'route', Controller]);