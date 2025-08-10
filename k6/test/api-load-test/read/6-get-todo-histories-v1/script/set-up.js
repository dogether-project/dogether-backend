import fs from 'fs';
import path from 'path';
import {insertData} from "../../../../../common/db/util/data-insert-runner.js";
import {
    createDummyData,
    getChallengeGroupIdsPerMember, getChallengeGroupMembersPerMember
} from "../../../../../common/db/data/set-up-data/set-up-data-1.js"

const TEMP_OUTPUT_PATH = path.join('./script/data.json');

async function setUp() {
    await insertData(createDummyData);
    const groupIds = getChallengeGroupIdsPerMember();
    const otherGroupMemberIds = getChallengeGroupMembersPerMember();

    try {
        fs.writeFileSync(TEMP_OUTPUT_PATH, JSON.stringify({ groupIds, otherGroupMemberIds }, null, 2), 'utf-8');
        console.log(`✅ data.json 저장 완료!\n`);
    } catch (err) {
        console.error('❌ 파일 저장 실패\n');
        throw err;
    }
}

setUp();
