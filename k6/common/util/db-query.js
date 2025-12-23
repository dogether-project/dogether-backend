import {batchInsert} from "./db-util.js";

// =========== 테이블 데이터 insert 쿼리 ===========
/**
 * challenge_group 테이블 데이터 insert 쿼리
 */
export async function insertChallengeGroup(connection, insertData, batchSize) {
    console.log(`✏️ challenge_group 테이블에 데이터 ${insertData.length}건 삽입 시작.`);

    const query = `
        INSERT INTO challenge_group (
            id,
            name,
            maximum_member_count,
            join_code,
            status,
            start_at,
            end_at,
            created_at,
            row_inserted_at,
            row_updated_at                
        ) VALUES ?
    `;

    await batchInsert(
        connection,
        query,
        insertData,
        batchSize,
        'challenge_group'
    );
}

/**
 * challenge_group_member 테이블 데이터 insert 쿼리
 */
export async function insertChallengeGroupMember(connection, insertData, batchSize) {
    console.log(`✏️ challenge_group_member 테이블에 데이터 ${insertData.length}건 삽입 시작.`);

    const query = `
        INSERT INTO challenge_group_member (
            id,
            challenge_group_id,
            member_id,
            created_at,
            row_inserted_at,
            row_updated_at                
        ) VALUES ?
    `;

    await batchInsert(
        connection,
        query,
        insertData,
        batchSize,
        'challenge_group_member'
    );
}

/**
 * last_selected_challenge_group_record 테이블 데이터 insert 쿼리
 */
export async function insertLastSelectedChallengeGroupRecord(connection, insertData, batchSize) {
    console.log(`✏️ last_selected_challenge_group_record 테이블에 데이터 ${insertData.length}건 삽입 시작.`);

    const query = `
        INSERT INTO last_selected_challenge_group_record (
            id,
            challenge_group_id,
            member_id,
            row_inserted_at,
            row_updated_at                
        ) VALUES ?
    `;

    await batchInsert(
        connection,
        query,
        insertData,
        batchSize,
        'last_selected_challenge_group_record'
    );
}

/**
 * daily_todo 테이블 데이터 insert 쿼리
 */
export async function insertDailyTodo(connection, insertData, batchSize) {
    console.log(`✏️ daily_todo 테이블에 데이터 ${insertData.length}건 삽입 시작.`);

    const query = `
        INSERT INTO daily_todo (
            id,
            challenge_group_id,
            writer_id,
            content,
            status,
            written_at,
            row_inserted_at,
            row_updated_at                
        ) VALUES ?
    `;

    await batchInsert(
        connection,
        query,
        insertData,
        batchSize,
        'daily_todo'
    );
}

/**
 * daily_todo_history 테이블 데이터 insert 쿼리
 */
export async function insertDailyTodoHistory(connection, insertData, batchSize) {
    console.log(`✏️ daily_todo_history 테이블에 데이터 ${insertData.length}건 삽입 시작.`);

    const query = `
        INSERT INTO daily_todo_history (
            id,
            daily_todo_id,
            event_time,
            row_inserted_at,
            row_updated_at                
        ) VALUES ?
    `;

    await batchInsert(
        connection,
        query,
        insertData,
        batchSize,
        'daily_todo_history'
    );
}

/**
 * daily_todo_history_read 테이블 데이터 insert 쿼리
 */
export async function insertDailyTodoHistoryRead(connection, insertData, batchSize) {
    console.log(`✏️ daily_todo_history_read 테이블에 데이터 ${insertData.length}건 삽입 시작.`);

    const query = `
        INSERT INTO daily_todo_history_read (
            id,
            member_id,
            daily_todo_history_id,
            row_inserted_at,
            row_updated_at                
        ) VALUES ?
    `;

    await batchInsert(
        connection,
        query,
        insertData,
        batchSize,
        'daily_todo_history_read'
    );
}

/**
 * daily_todo_certification 테이블 데이터 insert 쿼리
 */
export async function insertDailyTodoCertification(connection, insertData, batchSize) {
    console.log(`✏️ daily_todo_certification 테이블에 데이터 ${insertData.length}건 삽입 시작.`);

    const query = `
        INSERT INTO daily_todo_certification (
            id,
            daily_todo_id,
            content,
            media_url,
            review_status,
            review_feedback,
            created_at,
            row_inserted_at,
            row_updated_at                
        ) VALUES ?
    `;

    await batchInsert(
        connection,
        query,
        insertData,
        batchSize,
        'daily_todo_certification'
    );
}

/**
 * daily_todo_certification_reviewer 테이블 데이터 insert 쿼리
 */
export async function insertDailyTodoCertificationReviewer(connection, insertData, batchSize) {
    console.log(`✏️ daily_todo_certification_reviewer 테이블에 데이터 ${insertData.length}건 삽입 시작.`);

    const query = `
        INSERT INTO daily_todo_certification_reviewer (
            id,
            daily_todo_certification_id,
            reviewer_id,
            row_inserted_at,
            row_updated_at                
        ) VALUES ?
    `;

    await batchInsert(
        connection,
        query,
        insertData,
        batchSize,
        'daily_todo_certification_reviewer'
    );
}

// =========== 테이블 데이터 delete 쿼리 ===========
export async function deleteAllByRowInsertedAt(connection, table, date){
    try {
        await connection.query("SET FOREIGN_KEY_CHECKS = 0");

        const column = "row_inserted_at";
        const [cntRows] = await connection.query(
            `SELECT COUNT(*) AS c
             FROM \`${table}\`
             WHERE DATE (\`${column}\`) = ?`,
            [date]
        );
        const count = cntRows[0]?.c ?? 0;

        if (count === 0) {
            console.log(`⏭️ ${table} : 삭제할 행 없음 (DATE(${column}) = ${date})\n`);
            return 0;
        }

        const [res] = await connection.query(
            `DELETE
             FROM \`${table}\`
             WHERE DATE(\`${column}\`) = ?`,
            [date]
        );

        console.log(`🗑️ ${table} : ${res.affectedRows}건 삭제 완료! (DATE(${column}) = ${date})\n`);
        return res.affectedRows ?? 0;
    } catch (err) {
        console.error("❌ 데이터 삭제중 오류 발생");
        throw err;
    } finally {
        await connection.query("SET FOREIGN_KEY_CHECKS = 1");
    }
}

// =========== 테이블 생성 & 삭제 쿼리 ===========
/**
 * dogether 스키마의 모든 테이블 생성
 */
export async function createAllTable(connection) {
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
        console.log(`🏠 dogether 스키마의 모든 테이블 생성중... (생성 테이블 개수 : ${createStatements.length})`);
        // DDL은 트랜잭션 경계에 영향받지 않으니 FK만 꺼서 순서 보장
        await connection.query("SET FOREIGN_KEY_CHECKS = 0");

        // CREATE
        for (const ddl of createStatements) {
            await connection.query(ddl);
            const match = ddl.match(/create table\s+([^\s(]+)/i);
            const created = match ? match[1] : "(unknown)";
            console.log(`🆕 Created table : ${created}`);
        }

        console.error("✅ 테이블 생성 완료!\n");
    } catch (err) {
        console.error("❌ 테이블 생성중 오류 발생");
        throw err;
    } finally {
        await connection.query("SET FOREIGN_KEY_CHECKS = 1");
    }
}

/**
 * dogether 스키마의 모든 테이블 삭제
 */
export async function removeAllTable(connection) {
    // 자식 → 부모 순서로 DROP
    const tables = [
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

    try {
        console.log(`🧨 dogether 스키마의 모든 테이블 삭제중... (삭제 테이블 개수 : ${tables.length})`);
        // DDL은 트랜잭션 경계에 영향받지 않으니 FK만 꺼서 순서 보장
        await connection.query("SET FOREIGN_KEY_CHECKS = 0");

        // DROP Table
        for (const tbl of tables) {
            await connection.query(`DROP TABLE IF EXISTS ${tbl}`);
            console.log(`🗑️ Dropped table : ${tbl}`);
        }

        console.log("✅ 모든 테이블 삭제 완료!\n");
    } catch (err) {
        console.error("❌ 테이블 삭제중 오류 발생");
        throw err;
    } finally {
        await connection.query("SET FOREIGN_KEY_CHECKS = 1");
    }
}
