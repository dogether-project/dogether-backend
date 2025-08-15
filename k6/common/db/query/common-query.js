export const deleteAllByRowInsertedAt = async (connection, table, date, column) => {
    const [cntRows] = await connection.query(
        `SELECT COUNT(*) AS c FROM \`${table}\` WHERE DATE(\`${column}\`) = ?`,
        [date]
    );
    const count = cntRows[0]?.c ?? 0;

    if (count === 0) {
        console.log(`⏭️ ${table} : 삭제할 행 없음 (DATE(${column}) = ${date})\n`);
        return 0;
    }

    const [res] = await connection.query(
        `DELETE FROM \`${table}\` WHERE DATE(\`${column}\`) = ?`,
        [date]
    );

    console.log(`🗑️ ${table} : ${res.affectedRows}건 삭제 완료! (DATE(${column}) = ${date})\n`);
    return res.affectedRows ?? 0;
}
