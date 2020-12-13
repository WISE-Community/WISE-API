'use strict';

import * as angular from 'angular';
import { downgradeInjectable } from '@angular/upgrade/static';
import { OpenResponseService } from './openResponseService';
import OpenResponseAuthoring from './openResponseAuthoring';

const openResponseAuthoringComponentModule = angular
  .module('openResponseAuthoringComponentModule', ['pascalprecht.translate'])
  .service('OpenResponseService', downgradeInjectable(OpenResponseService))
  .component('openResponseAuthoring', OpenResponseAuthoring)
  .config([
    '$translatePartialLoaderProvider',
    $translatePartialLoaderProvider => {
      $translatePartialLoaderProvider.addPart('components/openResponse/i18n');
    }
  ]);

export default openResponseAuthoringComponentModule;
