package com.bboss.cache.validDao.interfaces;

import com.bboss.cache.bean.approve.ApproveSyncLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IApproveSyncLogDao {

    int insert(ApproveSyncLog record);

    int batchInsert(List<ApproveSyncLog> list);

    List<ApproveSyncLog> selectApproveSyncLogByTrans(@Param("transIDO") String transIDO);

    List<ApproveSyncLog> selectApproveSyncLogs(@Param("transId") String transId);
}
