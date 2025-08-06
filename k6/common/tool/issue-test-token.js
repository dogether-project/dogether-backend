import axios from 'axios';
import fs from 'fs';
import path from 'path';
import { API_BASE_URL } from '../../secret/secret.js';

const TOKEN_OUTPUT_PATH = path.join('../../secret/tokens.json');
const TEST_MEMBER_COUNT = 100;

async function issueTestTokens() {
    const testMemberIds = [];
    for (let i = 0; i < TEST_MEMBER_COUNT; i++) {
        testMemberIds.push(i + 1);
    }

    try {
        const res = await axios.post(`${API_BASE_URL}/api/dev/issue-test-token`, { memberIds: testMemberIds});
        const testTokens = res.data;

        fs.writeFileSync(TOKEN_OUTPUT_PATH, JSON.stringify(testTokens, null, 2), 'utf-8');

        console.log(`\n🎉 총 ${testMemberIds.length}개의 테스트 유저 토큰을 생성하였습니다.\n`);
    } catch (error) {
        console.error(`❌ 테스트 유저 토큰 발급 실패`);
        console.error(error);
    }
}

issueTestTokens();
