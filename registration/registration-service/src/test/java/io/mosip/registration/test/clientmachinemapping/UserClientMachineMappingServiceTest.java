package io.mosip.registration.test.clientmachinemapping;

import static org.mockito.Mockito.doNothing;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.core.spi.logger.MosipLogger;
import io.mosip.kernel.logger.appender.MosipRollingFileAppender;
import io.mosip.registration.audit.AuditFactory;
import io.mosip.registration.constants.AppModuleEnum;
import io.mosip.registration.constants.AuditEventEnum;
import io.mosip.registration.constants.RegConstants;
import io.mosip.registration.dao.MachineMappingDAO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.SuccessResponseDTO;
import io.mosip.registration.dto.UserMachineMappingDTO;
import io.mosip.registration.entity.RegistrationUserDetail;
import io.mosip.registration.entity.RegistrationUserRole;
import io.mosip.registration.entity.RegistrationUserRoleID;
import io.mosip.registration.entity.UserMachineMapping;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.service.MapMachineServiceImpl;
import io.mosip.registration.util.mac.SystemMacAddress;
import io.mosip.registration.entity.UserMachineMappingID;

public class UserClientMachineMappingServiceTest {

	@Mock
	MachineMappingDAO machineMappingDAO;

	@Mock
	MosipLogger logger;
	private MosipRollingFileAppender mosipRollingFileAppender;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@InjectMocks
	MapMachineServiceImpl mapMachineServiceImpl;

	@Mock
	private AuditFactory auditFactory;

	@Before
	public void initialize() throws IOException, URISyntaxException {
		mosipRollingFileAppender = new MosipRollingFileAppender();
		mosipRollingFileAppender.setAppenderName("org.apache.log4j.RollingFileAppender");
		mosipRollingFileAppender.setFileName("logs");
		mosipRollingFileAppender.setFileNamePattern("logs/registration-processor-%d{yyyy-MM-dd-HH-mm}-%i.log");
		mosipRollingFileAppender.setMaxFileSize("1MB");
		mosipRollingFileAppender.setTotalCap("10MB");
		mosipRollingFileAppender.setMaxHistory(10);
		mosipRollingFileAppender.setImmediateFlush(true);
		mosipRollingFileAppender.setPrudent(true);

		ReflectionTestUtils.setField(RegBaseUncheckedException.class, "LOGGER", logger);
		ReflectionTestUtils.setField(RegBaseCheckedException.class, "LOGGER", logger);
		ReflectionTestUtils.invokeMethod(mapMachineServiceImpl, "initializeLogger", mosipRollingFileAppender);
		ReflectionTestUtils.setField(mapMachineServiceImpl, "LOGGER", logger);
		doNothing().when(logger).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString());
		doNothing().when(auditFactory).audit(Mockito.any(AuditEventEnum.class), Mockito.any(AppModuleEnum.class),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
	}

	@Test
	public void view() throws RegBaseCheckedException {

		ResponseDTO responseDTO = new ResponseDTO();
		String machineID = SystemMacAddress.getSystemMacAddress();

		Mockito.when(machineMappingDAO.getStationID(Mockito.anyString())).thenReturn("StationID");
 
		Mockito.when(machineMappingDAO.getCenterID(Mockito.anyString())).thenReturn("CenterID107");

		List<RegistrationUserDetail> userDetailsList = new ArrayList<>();

		UserMachineMappingID machineMappingID = new UserMachineMappingID();
		machineMappingID.setUserID("ID123456");
		machineMappingID.setMachineID(machineID);

		UserMachineMapping userMachineMapping = new UserMachineMapping();
		userMachineMapping.setIsActive(true);
		userMachineMapping.setUserMachineMappingId(machineMappingID);

		RegistrationUserRoleID registrationUserRoleID = new RegistrationUserRoleID();
		registrationUserRoleID.setRoleCode("101");

		RegistrationUserRole registrationUserRole = new RegistrationUserRole();
		registrationUserRole.setRegistrationUserRoleID(registrationUserRoleID);

		Set<RegistrationUserRole> userRole = new HashSet();
		userRole.add(registrationUserRole);

		Set<UserMachineMapping> userMachine = new HashSet();
		userMachine.add(userMachineMapping);

		RegistrationUserDetail registrationUserDetail = new RegistrationUserDetail();
		registrationUserDetail.setCntrId("CenterID123");
		registrationUserDetail.setId("ID123456");
		registrationUserDetail.setName("Registration");
		registrationUserDetail.setUserMachineMapping(userMachine);
		registrationUserDetail.setUserRole(userRole);
		userDetailsList.add(registrationUserDetail);

		Mockito.when(machineMappingDAO.getUsers(Mockito.anyString())).thenReturn(userDetailsList);

		ResponseDTO res = mapMachineServiceImpl.view();

		Assert.assertSame("User Data Fetched Successfully", res.getSuccessResponseDTO().getMessage());
	}

	@Test
	public void viewFailureTest() throws RegBaseCheckedException {
		RegBaseCheckedException baseCheckedException=new RegBaseCheckedException("101","No record Found");		
		Mockito.when(machineMappingDAO.getStationID(Mockito.anyString())).thenReturn(baseCheckedException.getMessage());
		ResponseDTO res=mapMachineServiceImpl.view();
		Assert.assertSame("No Records Found",res.getErrorResponseDTOs().get(0).getMessage());
	}
	
	@Test
	public void updateTest() {
		ReflectionTestUtils.setField(mapMachineServiceImpl, "LOGGER", logger);

		doNothing().when(logger).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString());

		UserMachineMappingDTO machineMappingDTO = new UserMachineMappingDTO("ID123", "Nm123", "ADmin", "ACTIVE",
				"CNTR123", "STN123", "MCHN123");
		UserMachineMapping user = new UserMachineMapping();

		ResponseDTO responseDTO = new ResponseDTO();
		SuccessResponseDTO successResponseDTO = new SuccessResponseDTO();
		successResponseDTO.setCode(RegConstants.MACHINE_MAPPING_CODE);
		successResponseDTO.setInfoType(RegConstants.ALERT_INFORMATION);
		successResponseDTO.setMessage(RegConstants.MACHINE_MAPPING_SUCCESS_MESSAGE);
		responseDTO.setSuccessResponseDTO(successResponseDTO);

		Mockito.when(machineMappingDAO.update(Mockito.any(UserMachineMapping.class)))
				.thenReturn(RegConstants.MACHINE_MAPPING_UPDATED);
		Mockito.when(machineMappingDAO.findByID(Mockito.any())).thenReturn(null);

		Assert.assertSame(mapMachineServiceImpl.saveOrUpdate(machineMappingDTO).getSuccessResponseDTO().getMessage(),
				responseDTO.getSuccessResponseDTO().getMessage());
	}

	@Test
	public void saveTest() {
		ReflectionTestUtils.setField(mapMachineServiceImpl, "LOGGER", logger);

		doNothing().when(logger).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString());

		UserMachineMappingDTO machineMappingDTO = new UserMachineMappingDTO("ID123", "Nm123", "ADmin", "IN-ACTIVE",
				"CNTR123", "STN123", "MCHN123");
		UserMachineMapping user = new UserMachineMapping();

		ResponseDTO responseDTO = new ResponseDTO();
		SuccessResponseDTO successResponseDTO = new SuccessResponseDTO();
		successResponseDTO.setCode(RegConstants.MACHINE_MAPPING_CODE);
		successResponseDTO.setInfoType(RegConstants.ALERT_INFORMATION);
		successResponseDTO.setMessage(RegConstants.MACHINE_MAPPING_SUCCESS_MESSAGE);
		responseDTO.setSuccessResponseDTO(successResponseDTO);

		Mockito.when(machineMappingDAO.save(Mockito.any(UserMachineMapping.class)))
				.thenReturn(RegConstants.MACHINE_MAPPING_UPDATED);
		Mockito.when(machineMappingDAO.findByID(Mockito.any())).thenReturn(user);

		Assert.assertSame(mapMachineServiceImpl.saveOrUpdate(machineMappingDTO).getSuccessResponseDTO().getMessage(),
				responseDTO.getSuccessResponseDTO().getMessage());
	}

	@Test
	public void saveOrUpdateFailureTest() {
		ReflectionTestUtils.setField(mapMachineServiceImpl, "LOGGER", logger);

		doNothing().when(logger).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString());

		UserMachineMappingDTO machineMappingDTO = new UserMachineMappingDTO("ID123", "Nm123", "ADmin", "IN-ACTIVE",
				"CNTR123", "STN123", "MCHN123");
		UserMachineMapping user = new UserMachineMapping();

		ResponseDTO responseDTO = new ResponseDTO();
		SuccessResponseDTO successResponseDTO = new SuccessResponseDTO();
		successResponseDTO.setCode(RegConstants.MACHINE_MAPPING_CODE);
		successResponseDTO.setInfoType(RegConstants.ALERT_INFORMATION);
		successResponseDTO.setMessage(RegConstants.MACHINE_MAPPING_SUCCESS_MESSAGE);
		responseDTO.setSuccessResponseDTO(successResponseDTO);

		Mockito.when(machineMappingDAO.findByID(Mockito.any())).thenThrow(RegBaseUncheckedException.class);
		Assert.assertSame(
				mapMachineServiceImpl.saveOrUpdate(machineMappingDTO).getErrorResponseDTOs().get(0).getMessage(),
				"Unable to map user");
	}
}
