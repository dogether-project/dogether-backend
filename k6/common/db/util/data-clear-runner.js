import {
    createLocalDbConnection,
    createSshTunnelDbConnection,
    getCurrentDateInKstWithoutTime
} from "./db-util.js";
import {deleteAllByRowInsertedAt} from "../query/common-query.js";

export async function removeAllTestDataCreateToday() {
    const connection = await createLocalDbConnection(); // Local DB 커넥션
// const connection = await createSshTunnelDbConnection(); // AWS DB 커넥션

    const date = getCurrentDateInKstWithoutTime();
    const steps = [
        { table: "daily_todo_certification_reviewer", col: "row_inserted_at" },
        { table: "daily_todo_certification",          col: "row_inserted_at" },
        { table: "daily_todo_history",                col: "row_inserted_at" },
        { table: "daily_todo",                         col: "row_inserted_at" },
        { table: "last_selected_challenge_group_record", col: "row_inserted_at" },
        { table: "challenge_group_member",            col: "row_inserted_at" },
        { table: "challenge_group",                   col: "row_inserted_at" },
        { table: "daily_todo_stats",                  col: "row_inserted_at" },
        { table: "notification_token",                col: "row_inserted_at" },
        { table: "member",                             col: "row_inserted_at" },
    ];

    try {
        console.log(`👷 K6 테스트 데이터 삭제 시작.\n`)
        await connection.beginTransaction();

        for (const s of steps) {
            await deleteAllByRowInsertedAt(connection, s.table, date, s.col);
        }

        await connection.commit();
        console.log("🧹 K6 테스트 데이터 정리 완료!\n");
    } catch (err) {
        await connection.rollback();
        console.error("❌ K6 테스트 데이터 정리 중 오류 발생, 롤백 수행 완료.\n");
        throw err;
    } finally {
        await connection.end();
    }
}

export async function clearAndReCreateAllTable() {
    const connection = await createLocalDbConnection(); // Local DB 커넥션
// const connection = await createSshTunnelDbConnection(); // AWS DB 커넥션

    console.log("🧨 모든 테이블 DROP & CREATE 시작!\n");

    // 자식 → 부모 순서로 DROP
    const dropOrder = [
        "dogether.daily_todo_certification_reviewer",
        "dogether.daily_todo_certification",
        "dogether.daily_todo_history_read",
        "dogether.daily_todo_history",
        "dogether.daily_todo",
        "dogether.last_selected_challenge_group_record",
        "dogether.challenge_group_member",
        "dogether.notification_token",
        "dogether.daily_todo_stats",
        "dogether.challenge_group",
        "dogether.member",
    ];

    // 부모 → 자식 순서로 CREATE (네가 준 DDL 그대로)
    const createStatements = [
        `create table dogether.member
    (
        id                bigint auto_increment primary key,
        row_inserted_at   datetime(6)  not null,
        row_updated_at    datetime(6)  null,
        created_at        datetime(6)  not null,
        name              varchar(20)  not null,
        profile_image_url varchar(500) not null,
        provider_id       varchar(100) not null,
        constraint UKq4jvd8lnevoqq74bkjcm3p6ub unique (provider_id)
    )`,

        `create table dogether.notification_token
    (
        id              bigint auto_increment primary key,
        row_inserted_at datetime(6)  not null,
        row_updated_at  datetime(6)  null,
        token_value     varchar(500) not null,
        member_id       bigint       null,
        constraint UKhkpuymswoyw1snef8lq0qy7ym unique (token_value),
        constraint FKqld7c8jfn885g6opo7e8f864k foreign key (member_id) references dogether.member (id)
    )`,

        `create table dogether.daily_todo_stats
    (
        id                 bigint auto_increment primary key,
        row_inserted_at    datetime(6)   not null,
        row_updated_at     datetime(6)   null,
        approved_count     int default 0 not null,
        certificated_count int default 0 not null,
        rejected_count     int default 0 not null,
        member_id          bigint        null,
        constraint UKoy9sit356flut425iai2mytei unique (member_id),
        constraint FKf7v9c69csmugocrkia40o9sxy foreign key (member_id) references dogether.member (id)
    )`,

        `create table dogether.challenge_group
    (
        id                   bigint auto_increment primary key,
        row_inserted_at      datetime(6)                                    not null,
        row_updated_at       datetime(6)                                    null,
        created_at           datetime(6)                                    not null,
        end_at               date                                           not null,
        join_code            varchar(20)                                    not null,
        maximum_member_count int                                            not null,
        name                 varchar(30)                                    not null,
        start_at             date                                           not null,
        status               enum ('D_DAY', 'FINISHED', 'READY', 'RUNNING') not null,
        constraint UK4u1imex81d8230pwke49ayhxf unique (join_code)
    )`,

        `create table dogether.challenge_group_member
    (
        id                 bigint auto_increment primary key,
        row_inserted_at    datetime(6) not null,
        row_updated_at     datetime(6) null,
        created_at         datetime(6) not null,
        challenge_group_id bigint      null,
        member_id          bigint      null,
        constraint UKi1ys69wj0kdw3rmqhcdkhiymt unique (challenge_group_id, member_id),
        constraint FK6by3tkxjgvlumrox2nw4nfed8 foreign key (member_id) references dogether.member (id),
        constraint FKtmjk04yhxvcw5t55ws9cc3ssv foreign key (challenge_group_id) references dogether.challenge_group (id)
    )`,

        `create table dogether.last_selected_challenge_group_record
    (
        id                 bigint auto_increment primary key,
        row_inserted_at    datetime(6) not null,
        row_updated_at     datetime(6) null,
        challenge_group_id bigint      not null,
        member_id          bigint      not null,
        constraint FK64ou3obhysnh555oue796v3iu foreign key (member_id) references dogether.member (id),
        constraint FKt11andgmtafsp7k9calqb6jdu foreign key (challenge_group_id) references dogether.challenge_group (id)
    )`,

        `create table dogether.daily_todo
    (
        id                 bigint auto_increment primary key,
        row_inserted_at    datetime(6)                                   not null,
        row_updated_at     datetime(6)                                   null,
        content            varchar(400)                                  not null,
        status             enum ('CERTIFY_COMPLETED', 'CERTIFY_PENDING') not null,
        written_at         datetime(6)                                   not null,
        challenge_group_id bigint                                        not null,
        writer_id          bigint                                        not null,
        constraint FKaf8k4xsrps3qn2j6s24f7o0j6 foreign key (writer_id) references dogether.member (id),
        constraint FKk5afs3syryeg4i349pr7tia5k foreign key (challenge_group_id) references dogether.challenge_group (id)
    )`,

        `create table dogether.daily_todo_history
    (
        id              bigint auto_increment primary key,
        row_inserted_at datetime(6) not null,
        row_updated_at  datetime(6) null,
        event_time      datetime(6) not null,
        daily_todo_id   bigint      not null,
        constraint UKhe6eyyn35yhdg973vjboe1gdg unique (daily_todo_id),
        constraint FK2shj5w1xxje8ftxdii1eqvi8j foreign key (daily_todo_id) references dogether.daily_todo (id)
    )`,

        `create table dogether.daily_todo_history_read
    (
        id                    bigint auto_increment primary key,
        row_inserted_at       datetime(6) not null,
        row_updated_at        datetime(6) null,
        daily_todo_history_id bigint      not null,
        member_id             bigint      not null,
        constraint FKifwgtd8i4sskt5ka01mfif5cu foreign key (daily_todo_history_id) references dogether.daily_todo_history (id),
        constraint FKoeku89lvgqokbpqe22gaylxt8 foreign key (member_id) references dogether.member (id)
    )`,

        `create table dogether.daily_todo_certification
    (
        id              bigint auto_increment primary key,
        row_inserted_at datetime(6)                                  not null,
        row_updated_at  datetime(6)                                  null,
        content         varchar(500)                                 not null,
        created_at      datetime(6)                                  not null,
        media_url       varchar(500)                                 not null,
        review_feedback varchar(800)                                 null,
        review_status   enum ('APPROVE', 'REJECT', 'REVIEW_PENDING') not null,
        daily_todo_id   bigint                                       not null,
        constraint UK7eonlvyatp6kur1noici8hbfo unique (daily_todo_id),
        constraint FKrcnfuxppehjdad3rfyl5wuy4s foreign key (daily_todo_id) references dogether.daily_todo (id)
    )`,

        `create table dogether.daily_todo_certification_reviewer
    (
        id                          bigint auto_increment primary key,
        row_inserted_at             datetime(6) not null,
        row_updated_at              datetime(6) null,
        daily_todo_certification_id bigint      not null,
        reviewer_id                 bigint      not null,
        constraint UKe63rha6d3bdo0gc2xf4glm38n unique (daily_todo_certification_id),
        constraint FK9mt7av8nwaej3knib2dtw5vsw foreign key (reviewer_id) references dogether.member (id),
        constraint FKt6p7r92873o111xlangbs8ya3 foreign key (daily_todo_certification_id) references dogether.daily_todo_certification (id)
    )`,
    ];

    try {
        // DDL은 트랜잭션 경계에 영향받지 않으니 FK만 꺼서 순서 보장
        await connection.query("SET FOREIGN_KEY_CHECKS = 0");

        // DROP
        for (const tbl of dropOrder) {
            await connection.query(`DROP TABLE IF EXISTS ${tbl}`);
            console.log(`🗑️ Dropped table : ${tbl}`);
        }
        console.log();

        // CREATE
        for (const ddl of createStatements) {
            await connection.query(ddl);
            const match = ddl.match(/create table\s+([^\s(]+)/i);
            const created = match ? match[1] : "(unknown)";
            console.log(`🆕 Created table : ${created}`);
        }
        console.log();

        await connection.query("SET FOREIGN_KEY_CHECKS = 1");
        console.log("✅ 모든 테이블 재생성 완료! (AUTO_INCREMENT 리셋)\n");
    } catch (err) {
        // 혹시 에러 시 FK 다시 켜줌
        try { await connection.query("SET FOREIGN_KEY_CHECKS = 1"); } catch {}
        console.error("❌ 테이블 재생성 중 오류 발생\n");
        throw err;
    } finally {
        // 에러든 성공이든 FK 체크는 반드시 켜두고 연결 종료
        try { await connection.query("SET FOREIGN_KEY_CHECKS = 1"); } catch {}
        await connection.end();
    }
}
