create table teacher_presentation_config (
    id bigint not null auto_increment,
    runId bigint not null,
    periodId bigint not null,
    nodeId varchar(30) not null,
    componentId varchar(30) not null,
    componentType varchar(30) not null,
    items text not null,
    studentNamesDisplay varchar(30) not null default 'hide',
    prompt text,
    updatedByWorkgroupId bigint,
    createdAt datetime(3) not null,
    updatedAt datetime(3) not null,
    primary key (id),
    unique key teacherPresentationConfigUniqueKey (runId, periodId, nodeId, componentId),
    index teacherPresentationConfigRunIdIndex (runId),
    constraint teacherPresentationConfigRunIdFK foreign key (runId) references runs (id),
    constraint teacherPresentationConfigPeriodIdFK foreign key (periodId) references `groups` (id),
    constraint teacherPresentationConfigUpdatedByWorkgroupIdFK foreign key (updatedByWorkgroupId) references workgroups (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table teacher_presentation_reflection_answers (
    id bigint not null auto_increment,
    teacherPresentationConfigId bigint not null,
    questionId varchar(30) not null,
    questionText text not null,
    answerText text,
    answeredByWorkgroupId bigint,
    createdAt datetime(3) not null,
    updatedAt datetime(3) not null,
    primary key (id),
    unique key teacherPresentationReflectionAnswersUniqueKey (teacherPresentationConfigId, questionId),
    constraint teacherPresentationReflectionAnswersConfigIdFK foreign key (teacherPresentationConfigId) references teacher_presentation_config (id) on delete cascade,
    constraint teacherPresentationReflectionAnswersWorkgroupIdFK foreign key (answeredByWorkgroupId) references workgroups (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
