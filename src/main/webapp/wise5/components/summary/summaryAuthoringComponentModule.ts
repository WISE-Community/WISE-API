'use strict';

import * as angular from 'angular';
import { downgradeInjectable } from '@angular/upgrade/static';
import { SummaryService } from './summaryService';
import SummaryController from './summaryController';
import SummaryAuthoringController from './summaryAuthoringController';

const summaryAuthoringComponentModule = angular
  .module('summaryAuthoringComponentModule', ['pascalprecht.translate'])
  .service('SummaryService', downgradeInjectable(SummaryService))
  .controller('SummaryController', SummaryController)
  .controller('SummaryAuthoringController', SummaryAuthoringController)
  .config([
    '$translatePartialLoaderProvider',
    $translatePartialLoaderProvider => {
      $translatePartialLoaderProvider.addPart('components/summary/i18n');
    }
  ]);

export default summaryAuthoringComponentModule;
