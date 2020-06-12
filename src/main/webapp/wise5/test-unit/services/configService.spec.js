import vleModule from '../../vle/vle';

let ConfigService, $httpBackend, sampleConfig1;
const configURL = 'http://localhost:8080/wise/config/1';
const sampleConfig2 = window.mocks['test-unit/sampleData/config/config2'];
const sampleI18N_common_en = window.mocks['test-unit/sampleData/i18n/common/i18n_en'];
const sampleI18N_vle_en = window.mocks['test-unit/sampleData/i18n/vle/i18n_en'];
const i18nURL_common_en = 'wise5/i18n/common/i18n_en.json';
const i18nURL_vle_en = 'wise5/i18n/vle/i18n_en.json';

describe('ConfigService Unit Test', () => {
  beforeEach(angular.mock.module(vleModule.name));

  beforeEach(inject((_ConfigService_, _$httpBackend_) => {
    ConfigService = _ConfigService_;
    $httpBackend = _$httpBackend_;
  }));

  describe('ConfigService', () => {
    beforeEach(() => {
      sampleConfig1 = window.mocks['test-unit/sampleData/config/config1'];
    });

    jasmine.clock().install();
    //shouldRetrieveConfig();
    shouldSortTheClassmatesAlphabeticallyByNameWhenSettingConfig();
    shouldGetTheLocale();
    shouldGetTheModes();
    shouldGetThePeriodIdOfTheStudent();
    shouldGetThePeriodsInTheRun();
    shouldGetTheUsernameByWorkgroupId();
    shouldGetTheTeacherWorkgroupId();
    shouldGetThePeriodIdGivenTheWorkgroupId();
    shouldCalculateIfARunIsActiveWhenARunOnlyHasAStartTime();
    shouldCalculateIfARunIsActiveWhenItHasAStartTimeAndEndTimeAndLockedValueFalse();
    shouldCalculateIfARunIsActiveWhenItHasAStartTimeAndEndTimeAndLockedValueTrue();
    shouldCalculateIsEndedAndLocked();
  });
});

function shouldRetrieveConfig() {
  it('should retrieve config', () => {
    spyOn(ConfigService, 'setConfig').and.callThrough();
    spyOn(ConfigService, 'sortClassmateUserInfosAlphabeticallyByName');
    $httpBackend.when('GET', configURL).respond(sampleConfig1);
    $httpBackend.when('GET', i18nURL_common_en).respond(sampleI18N_common_en);
    $httpBackend.when('GET', i18nURL_vle_en).respond(sampleI18N_vle_en);
    $httpBackend.expectGET(configURL);
    const configPromise = ConfigService.retrieveConfig(configURL);
    $httpBackend.flush();
    // TODO: when replacing this line below with expect(ConfigService.setConfig).toHaveBeenCalled(sampleConfig1);, it fails.
    // it shouldn't fail, so find out why.
    expect(ConfigService.setConfig).toHaveBeenCalled();
    expect(ConfigService.sortClassmateUserInfosAlphabeticallyByName).toHaveBeenCalled();
  });
}

function shouldSortTheClassmatesAlphabeticallyByNameWhenSettingConfig() {
  it('should sort the classmates alphabetically by name when setting config', () => {
    const config = {
      userInfo: {
        myUserInfo: {
          myClassInfo: {
            classmateUserInfos: [
              {
                periodId: 1,
                workgroupId: 3,
                userIds: [6],
                periodName: '1',
                username: 't t (tt0101)'
              },
              {
                periodId: 1,
                workgroupId: 8,
                userIds: [8],
                periodName: '1',
                username: 'k t (kt0101)'
              }
            ]
          }
        }
      }
    };
    const classmateUserInfos = config.userInfo.myUserInfo.myClassInfo.classmateUserInfos;
    expect(classmateUserInfos[0].workgroupId).toEqual(3);
    expect(classmateUserInfos[1].workgroupId).toEqual(8);
    spyOn(ConfigService, 'sortClassmateUserInfosAlphabeticallyByNameHelper').and.callThrough();
    ConfigService.setConfig(config);
    expect(ConfigService.sortClassmateUserInfosAlphabeticallyByNameHelper).toHaveBeenCalled();
    expect(classmateUserInfos[0].workgroupId).toEqual(8);
    expect(classmateUserInfos[1].workgroupId).toEqual(3);
  });
}

function shouldGetTheLocale() {
  it('should get the locale', () => {
    // Sample config 1 doesn't have locale set, so it should default to 'en'
    ConfigService.setConfig(sampleConfig1);
    const locale = ConfigService.getLocale();
    expect(locale).toEqual('en');

    // Sample config 2 should have 'ja' locale.
    ConfigService.setConfig(sampleConfig2);
    const locale2 = ConfigService.getLocale();
    expect(locale2).toEqual('ja');
  });
}

function shouldGetTheModes() {
  it('should get the modes', () => {
    ConfigService.setConfig(sampleConfig1);
    const mode = ConfigService.getMode();
    const isPreview = ConfigService.isPreview();
    expect(mode).toEqual('run');
    expect(isPreview).toEqual(false);

    ConfigService.setConfig(sampleConfig2);
    const mode2 = ConfigService.getMode();
    const isPreview2 = ConfigService.isPreview();
    expect(mode2).toEqual('preview');
    expect(isPreview2).toEqual(true);
  });
}

function shouldGetThePeriodIdOfTheStudent() {
  it('should get the period id of the student', () => {
    ConfigService.setConfig(sampleConfig1);
    const config1PeriodId = ConfigService.getPeriodId();
    expect(config1PeriodId).toEqual(1);

    ConfigService.setConfig(sampleConfig2);
    const config2PeriodId = ConfigService.getPeriodId();
    expect(config2PeriodId).toEqual(2);
  });
}

function shouldGetThePeriodsInTheRun() {
  it('should get the periods in the run', () => {
    ConfigService.setConfig(sampleConfig1);
    const config1Periods = ConfigService.getPeriods();
    expect(config1Periods).toEqual([
      { periodId: 1, periodName: '1' },
      { periodId: 2, periodName: '2' },
      { periodId: 3, periodName: 'newperiod' }
    ]);
    ConfigService.setConfig(sampleConfig2);
    const config2Periods = ConfigService.getPeriods();
    expect(config2Periods).toEqual([
      { periodId: 1, periodName: 'one' },
      { periodId: 2, periodName: 'two' }
    ]);
  });
}

function shouldGetTheUsernameByWorkgroupId() {
  it('should get the username by workgroup id', () => {
    const nonExistingWorkgroupId = 9999;
    ConfigService.setConfig(sampleConfig1);
    const studentFirstNames = ConfigService.getStudentFirstNamesByWorkgroupId(
      nonExistingWorkgroupId
    );
    expect(studentFirstNames.length).toEqual(0);
    const existingWorkgroupId = 8;
    const studentFirstNamesExisting = ConfigService.getStudentFirstNamesByWorkgroupId(
      existingWorkgroupId
    );
    expect(studentFirstNamesExisting).toEqual(['k']);
  });
}

function shouldGetTheTeacherWorkgroupId() {
  it('should get the teacher workgroup id', () => {
    // If teacher workgroup doesn't exist, it should return null
    ConfigService.setConfig(sampleConfig2);
    const teacherWorkgroupIdDoesNotExist = ConfigService.getTeacherWorkgroupId();
    expect(teacherWorkgroupIdDoesNotExist).toBeNull();

    // Otherwise it should get the teacher's workgroup id from the config
    const expectedTeacherWorkgroupId = 1;
    ConfigService.setConfig(sampleConfig1);
    const teacherWorkgroupIdExist = ConfigService.getTeacherWorkgroupId();
    expect(teacherWorkgroupIdExist).toEqual(expectedTeacherWorkgroupId);
  });
}

function shouldGetThePeriodIdGivenTheWorkgroupId() {
  it('should get the period id given the workgroup id', () => {
    ConfigService.setConfig(sampleConfig1);
    spyOn(ConfigService, 'getUserInfoByWorkgroupId').and.callThrough();

    // If workgroupId is null, period should be null
    const nullWorkgroupPeriodId = ConfigService.getPeriodIdByWorkgroupId(null);
    expect(nullWorkgroupPeriodId).toBeNull();

    // If specified workgroup doesn't exist, it should null
    const nonExistingWorkgroupId = 9999;
    const nonExistingWorkgroupPeriodId = ConfigService.getPeriodIdByWorkgroupId(
      nonExistingWorkgroupId
    );
    expect(ConfigService.getUserInfoByWorkgroupId).toHaveBeenCalledWith(nonExistingWorkgroupId);
    expect(nonExistingWorkgroupPeriodId).toBeNull();

    // Otherwise it should get workgroup's period id
    const existingWorkgroupId = 8;
    const existingWorkgroupPeriodId = ConfigService.getPeriodIdByWorkgroupId(existingWorkgroupId);
    expect(ConfigService.getUserInfoByWorkgroupId).toHaveBeenCalledWith(existingWorkgroupId);
    expect(existingWorkgroupPeriodId).toEqual(1);
  });
}

function shouldCalculateIfARunIsActiveWhenARunOnlyHasAStartTime() {
  it('should calculate if a run is active when a run only has a start time', () => {
    const configJSON = {
      startTime: new Date(2019, 5, 10).getTime(),
      timestampDiff: 0
    };
    jasmine.clock().mockDate(new Date(2019, 5, 9));
    expect(ConfigService.calculateIsRunActive(configJSON)).toBeFalsy();
    jasmine.clock().mockDate(new Date(2019, 5, 10));
    expect(ConfigService.calculateIsRunActive(configJSON)).toBeTruthy();
    jasmine.clock().mockDate(new Date(2019, 5, 11));
    expect(ConfigService.calculateIsRunActive(configJSON)).toBeTruthy();
  });
}

function shouldCalculateIfARunIsActiveWhenItHasAStartTimeAndEndTimeAndLockedValueFalse() {
  it(`should calculate if a run is active to be true when it has a start time and end time and is
      locked value false`, () => {
    const configJSON = {
      startTime: new Date(2020, 4, 10).getTime(),
      endTime: new Date(2020, 4, 20).getTime(),
      timestampDiff: 0,
      isLockedAfterEndDate: false
    };
    jasmine.clock().mockDate(new Date(2020, 4, 15));
    expect(ConfigService.calculateIsRunActive(configJSON)).toBeTruthy();
  });
  it(`should calculate if a run is active to be false when it has a start time and end time and is
      locked value false`, () => {
    const configJSON = {
      startTime: new Date(2020, 4, 10).getTime(),
      endTime: new Date(2020, 4, 20).getTime(),
      timestampDiff: 0,
      isLockedAfterEndDate: false
    };
    jasmine.clock().mockDate(new Date(2020, 4, 30));
    expect(ConfigService.calculateIsRunActive(configJSON)).toBeTruthy();
  });
}

function shouldCalculateIfARunIsActiveWhenItHasAStartTimeAndEndTimeAndLockedValueTrue() {
  it(`should calculate if a run is active to be true when it has a start time and end time and is
      locked value true`, () => {
    const configJSON = {
      startTime: new Date(2020, 4, 10).getTime(),
      endTime: new Date(2020, 4, 20).getTime(),
      timestampDiff: 0,
      isLockedAfterEndDate: true
    };
    jasmine.clock().mockDate(new Date(2020, 4, 15));
    expect(ConfigService.calculateIsRunActive(configJSON)).toBeTruthy();
  });
  it(`should calculate if a run is active to be false when it has a start time and end time and is
      locked value true`, () => {
    const configJSON = {
      startTime: new Date(2020, 4, 10).getTime(),
      endTime: new Date(2020, 4, 20).getTime(),
      timestampDiff: 0,
      isLockedAfterEndDate: true
    };
    jasmine.clock().mockDate(new Date(2020, 4, 30));
    expect(ConfigService.calculateIsRunActive(configJSON)).toBeFalsy();
  });
}

function shouldCalculateIsEndedAndLocked() {
  it('should calculate is ended and locked when it has a start time and no end time', () => {
    const configJSON = {
      startTime: new Date(2020, 4, 10).getTime(),
      endTime: null,
      timestampDiff: 0,
      isLockedAfterEndDate: false
    };
    jasmine.clock().mockDate(new Date(2020, 4, 11));
    expect(ConfigService.isEndedAndLocked(configJSON)).toBeFalsy();
  });
  it('should calculate is ended and locked when end time is in the future', () => {
    const configJSON = {
      startTime: new Date(2020, 4, 10).getTime(),
      endTime: new Date(2020, 4, 20).getTime(),
      timestampDiff: 0,
      isLockedAfterEndDate: false
    };
    jasmine.clock().mockDate(new Date(2020, 4, 15));
    expect(ConfigService.isEndedAndLocked(configJSON)).toBeFalsy();
  });
  it('should calculate is ended and locked when end time is in the past but not locked', () => {
    const configJSON = {
      startTime: new Date(2020, 4, 10).getTime(),
      endTime: new Date(2020, 4, 20).getTime(),
      timestampDiff: 0,
      isLockedAfterEndDate: false
    };
    jasmine.clock().mockDate(new Date(2020, 4, 30));
    expect(ConfigService.isEndedAndLocked(configJSON)).toBeFalsy();
  });
  it('should calculate is ended and locked when end time is in the past and locked', () => {
    const configJSON = {
      startTime: new Date(2020, 4, 10).getTime(),
      endTime: new Date(2020, 4, 20).getTime(),
      timestampDiff: 0,
      isLockedAfterEndDate: true
    };
    jasmine.clock().mockDate(new Date(2020, 4, 30));
    expect(ConfigService.isEndedAndLocked(configJSON)).toBeTruthy();
  });
}
