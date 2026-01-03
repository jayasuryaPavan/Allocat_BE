/*
 * Copyright (c) 2024 Allocat. All rights reserved.
 * 
 * This software and associated documentation files (the "Software") are the
 * proprietary and confidential information of Allocat. Unauthorized copying,
 * modification, distribution, or use of this Software, via any medium, is
 * strictly prohibited without the express written permission of Allocat.
 */

package com.allocat.pos.service;

import com.allocat.auth.entity.User;
import com.allocat.auth.repository.StoreRepository;
import com.allocat.auth.repository.UserRepository;
import com.allocat.pos.entity.AssociateCredential;
import com.allocat.pos.entity.SalesPersonLogin;
import com.allocat.pos.entity.Shift;
import com.allocat.pos.entity.ShiftSwap;
import com.allocat.pos.repository.AssociateCredentialRepository;
import com.allocat.pos.repository.SalesPersonLoginRepository;
import com.allocat.pos.repository.ShiftRepository;
import com.allocat.pos.repository.ShiftSwapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing shifts, shift swaps, sales person logins, and associate
 * authentication
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final ShiftSwapRepository shiftSwapRepository;
    private final SalesPersonLoginRepository salesPersonLoginRepository;
    private final AssociateCredentialRepository associateCredentialRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ============ Shift Management ============

    @Transactional
    public Shift startShift(Long userId, Long storeId, BigDecimal startingCashAmount,
            LocalDateTime expectedStartTime, LocalDateTime expectedEndTime, String notes) {
        log.info("Starting shift for user: {} at store: {}", userId, storeId);

        if (!storeRepository.existsById(storeId)) {
            throw new RuntimeException("Store not found: " + storeId);
        }

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found: " + userId);
        }

        if (shiftRepository.hasActiveShift(userId)) {
            throw new RuntimeException("User already has an active shift");
        }

        LocalDate shiftDate = LocalDate.now();
        LocalDateTime startTime = LocalDateTime.now();

        Shift shift = Shift.builder()
                .storeId(storeId)
                .userId(userId)
                .shiftDate(shiftDate)
                .startedAt(startTime)
                .expectedStartTime(expectedStartTime != null ? expectedStartTime : startTime)
                .expectedEndTime(expectedEndTime)
                .startingCashAmount(startingCashAmount != null ? startingCashAmount : BigDecimal.ZERO)
                .status(Shift.ShiftStatus.ACTIVE)
                .notes(notes)
                .build();

        Shift savedShift = shiftRepository.save(shift);

        recordSalesPersonLogin(userId, storeId, savedShift.getId(), SalesPersonLogin.LoginType.SHIFT_START,
                null, null, null);

        log.info("Shift started successfully: {}", savedShift.getId());
        return savedShift;
    }

    @Transactional
    public Shift endShift(Long shiftId, Long endedByUserId, BigDecimal endingCashAmount,
            BigDecimal expectedCashAmount, String notes) {
        log.info("Ending shift: {} by user: {}", shiftId, endedByUserId);

        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found: " + shiftId));

        if (shift.getStatus() != Shift.ShiftStatus.ACTIVE) {
            throw new RuntimeException("Shift is not active. Current status: " + shift.getStatus());
        }

        LocalDateTime endTime = LocalDateTime.now();
        shift.setEndedAt(endTime);
        shift.setEndedBy(endedByUserId);
        shift.setEndingCashAmount(endingCashAmount);
        shift.setExpectedCashAmount(expectedCashAmount);

        if (expectedCashAmount != null && endingCashAmount != null) {
            shift.setCashDifference(endingCashAmount.subtract(expectedCashAmount));
        }

        shift.setStatus(Shift.ShiftStatus.COMPLETED);
        shift.setNotes(notes != null ? notes : shift.getNotes());

        Shift savedShift = shiftRepository.save(shift);

        recordSalesPersonLogin(shift.getUserId(), shift.getStoreId(), shiftId,
                SalesPersonLogin.LoginType.SHIFT_END, null, null, null);

        log.info("Shift ended successfully: {}", shiftId);
        return savedShift;
    }

    public Shift getActiveShift(Long storeId, Long userId) {
        return shiftRepository.findActiveShiftByStoreAndUser(storeId, userId)
                .orElseThrow(() -> new RuntimeException("No active shift found for user: " + userId));
    }

    public Shift getShiftById(Long shiftId) {
        return shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found: " + shiftId));
    }

    public List<Shift> getShiftsByDate(Long storeId, LocalDate date) {
        return shiftRepository.findByStoreIdAndShiftDate(storeId, date);
    }

    public List<Shift> getShiftsByStatus(Long storeId, Shift.ShiftStatus status) {
        return shiftRepository.findByStoreIdAndStatus(storeId, status);
    }

    public List<Shift> getActiveShifts(Long storeId) {
        return shiftRepository.findActiveShiftsByStore(storeId);
    }

    public List<Shift> getShiftsByDateRange(Long storeId, LocalDate startDate, LocalDate endDate) {
        return shiftRepository.findShiftsByDateRange(storeId, startDate, endDate);
    }

    // ============ Day Management ============

    @Transactional
    public void startNewDay(Long storeId, LocalDate date, BigDecimal initialCashAmount) {
        log.info("Starting new day for store: {} on date: {}", storeId, date);

        if (date == null) {
            date = LocalDate.now();
        }

        List<Shift> activeShifts = shiftRepository.findActiveShiftsByStore(storeId);
        if (!activeShifts.isEmpty()) {
            throw new RuntimeException("Cannot start new day. There are " + activeShifts.size() +
                    " active shifts. Please end all shifts first.");
        }

        log.info("New day started successfully for store: {}", storeId);
    }

    @Transactional
    public void endDay(Long storeId, LocalDate date, String notes) {
        log.info("Ending day for store: {} on date: {}", storeId, date);

        if (date == null) {
            date = LocalDate.now();
        }

        List<Shift> activeShifts = shiftRepository.findActiveShiftsByStore(storeId);
        if (!activeShifts.isEmpty()) {
            throw new RuntimeException("Cannot end day. There are " + activeShifts.size() +
                    " active shifts. Please end all shifts first.");
        }

        log.info("Day ended successfully for store: {}", storeId);
    }

    // ============ Shift Swap Management ============

    @Transactional
    public ShiftSwap createShiftSwap(Long requestedByUserId, Long originalShiftId, Long requestedToUserId,
            LocalDate originalShiftDate, LocalDate swapShiftDate, String reason) {
        log.info("Creating shift swap request from user: {} to user: {} for shift: {}",
                requestedByUserId, requestedToUserId, originalShiftId);

        Shift originalShift = shiftRepository.findById(originalShiftId)
                .orElseThrow(() -> new RuntimeException("Original shift not found: " + originalShiftId));

        userRepository.findById(requestedByUserId)
                .orElseThrow(() -> new RuntimeException("Requesting user not found: " + requestedByUserId));
        userRepository.findById(requestedToUserId)
                .orElseThrow(() -> new RuntimeException("Requested user not found: " + requestedToUserId));

        if (!originalShift.getUserId().equals(requestedByUserId)) {
            throw new RuntimeException("Shift does not belong to requesting user");
        }

        if (shiftSwapRepository.existsActiveSwapForShift(originalShiftId, originalShiftDate, swapShiftDate)) {
            throw new RuntimeException("An active swap request already exists for this shift");
        }

        ShiftSwap swap = ShiftSwap.builder()
                .storeId(originalShift.getStoreId())
                .originalShiftId(originalShiftId)
                .requestedByUserId(requestedByUserId)
                .requestedToUserId(requestedToUserId)
                .originalShiftDate(originalShiftDate)
                .swapShiftDate(swapShiftDate)
                .status(ShiftSwap.SwapStatus.PENDING)
                .reason(reason)
                .build();

        ShiftSwap savedSwap = shiftSwapRepository.save(swap);
        log.info("Shift swap request created: {}", savedSwap.getId());
        return savedSwap;
    }

    @Transactional
    public ShiftSwap approveShiftSwapByEmployee(Long swapId, Long userId) {
        log.info("Employee approving shift swap: {} by user: {}", swapId, userId);

        ShiftSwap swap = shiftSwapRepository.findById(swapId)
                .orElseThrow(() -> new RuntimeException("Shift swap not found: " + swapId));

        if (!swap.getRequestedToUserId().equals(userId)) {
            throw new RuntimeException("Only the requested employee can approve this swap");
        }

        if (swap.getStatus() != ShiftSwap.SwapStatus.PENDING) {
            throw new RuntimeException("Swap is not in pending status. Current status: " + swap.getStatus());
        }

        swap.setStatus(ShiftSwap.SwapStatus.APPROVED);
        swap.setApprovedAt(LocalDateTime.now());

        ShiftSwap savedSwap = shiftSwapRepository.save(swap);
        log.info("Shift swap approved by employee: {}", swapId);
        return savedSwap;
    }

    @Transactional
    public ShiftSwap approveShiftSwapByManager(Long swapId, Long managerId, String managerNotes) {
        log.info("Manager approving shift swap: {} by manager: {}", swapId, managerId);

        ShiftSwap swap = shiftSwapRepository.findById(swapId)
                .orElseThrow(() -> new RuntimeException("Shift swap not found: " + swapId));

        if (swap.getStatus() != ShiftSwap.SwapStatus.APPROVED) {
            throw new RuntimeException("Swap must be approved by employee first. Current status: " + swap.getStatus());
        }

        Shift originalShift = shiftRepository.findById(swap.getOriginalShiftId())
                .orElseThrow(() -> new RuntimeException("Original shift not found"));

        originalShift.setUserId(swap.getRequestedToUserId());
        originalShift.setNotes((originalShift.getNotes() != null ? originalShift.getNotes() + " " : "") +
                "[Swapped from user " + swap.getRequestedByUserId() + "]");
        shiftRepository.save(originalShift);

        swap.setStatus(ShiftSwap.SwapStatus.MANAGER_APPROVED);
        swap.setApprovedBy(managerId);
        swap.setManagerNotes(managerNotes);
        swap.setApprovedAt(LocalDateTime.now());

        ShiftSwap savedSwap = shiftSwapRepository.save(swap);
        log.info("Shift swap approved by manager: {}", swapId);
        return savedSwap;
    }

    @Transactional
    public ShiftSwap rejectShiftSwap(Long swapId, Long rejectedByUserId, String reason) {
        log.info("Rejecting shift swap: {} by user: {}", swapId, rejectedByUserId);

        ShiftSwap swap = shiftSwapRepository.findById(swapId)
                .orElseThrow(() -> new RuntimeException("Shift swap not found: " + swapId));

        if (swap.getStatus() == ShiftSwap.SwapStatus.MANAGER_APPROVED ||
                swap.getStatus() == ShiftSwap.SwapStatus.REJECTED) {
            throw new RuntimeException("Cannot reject swap. Current status: " + swap.getStatus());
        }

        swap.setStatus(ShiftSwap.SwapStatus.REJECTED);
        swap.setRejectedBy(rejectedByUserId);
        swap.setManagerNotes(reason);
        swap.setRejectedAt(LocalDateTime.now());

        ShiftSwap savedSwap = shiftSwapRepository.save(swap);
        log.info("Shift swap rejected: {}", swapId);
        return savedSwap;
    }

    @Transactional
    public ShiftSwap cancelShiftSwap(Long swapId, Long userId) {
        log.info("Cancelling shift swap: {} by user: {}", swapId, userId);

        ShiftSwap swap = shiftSwapRepository.findById(swapId)
                .orElseThrow(() -> new RuntimeException("Shift swap not found: " + swapId));

        if (!swap.getRequestedByUserId().equals(userId)) {
            throw new RuntimeException("Only the requester can cancel this swap");
        }

        if (swap.getStatus() == ShiftSwap.SwapStatus.MANAGER_APPROVED) {
            throw new RuntimeException("Cannot cancel an already approved swap");
        }

        swap.setStatus(ShiftSwap.SwapStatus.CANCELLED);
        ShiftSwap savedSwap = shiftSwapRepository.save(swap);
        log.info("Shift swap cancelled: {}", swapId);
        return savedSwap;
    }

    public List<ShiftSwap> getPendingSwapRequests(Long userId) {
        return shiftSwapRepository.findByRequestedToUserIdAndStatus(userId, ShiftSwap.SwapStatus.PENDING);
    }

    public List<ShiftSwap> getSwapRequestsByUser(Long userId) {
        return shiftSwapRepository.findByRequestedByUserId(userId);
    }

    public List<ShiftSwap> getSwapRequestsByStore(Long storeId) {
        return shiftSwapRepository.findByStoreId(storeId);
    }

    // ============ Sales Person Login Management ============

    @Transactional
    public SalesPersonLogin recordSalesPersonLogin(Long userId, Long storeId, Long shiftId,
            SalesPersonLogin.LoginType loginType,
            String deviceInfo, String ipAddress, String location) {
        SalesPersonLogin login = SalesPersonLogin.builder()
                .userId(userId)
                .storeId(storeId)
                .shiftId(shiftId)
                .loginTime(LocalDateTime.now())
                .loginType(loginType != null ? loginType : SalesPersonLogin.LoginType.SHIFT_START)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .location(location)
                .build();

        SalesPersonLogin savedLogin = salesPersonLoginRepository.save(login);
        log.info("Sales person login recorded: {} for user: {}", savedLogin.getId(), userId);
        return savedLogin;
    }

    @Transactional
    public SalesPersonLogin recordSalesPersonLogout(Long userId) {
        SalesPersonLogin login = salesPersonLoginRepository
                .findFirstByUserIdAndLogoutTimeIsNullOrderByLoginTimeDesc(userId)
                .orElseThrow(() -> new RuntimeException("No active login found for user: " + userId));

        login.setLogoutTime(LocalDateTime.now());
        SalesPersonLogin savedLogin = salesPersonLoginRepository.save(login);
        log.info("Sales person logout recorded for user: {}", userId);
        return savedLogin;
    }

    public List<SalesPersonLogin> getLoginHistory(Long userId, LocalDate date) {
        if (date != null) {
            return salesPersonLoginRepository.findLoginsByUserAndDate(userId, date);
        }
        return salesPersonLoginRepository.findByUserIdAndLogoutTimeIsNull(userId);
    }

    public List<SalesPersonLogin> getLoginHistoryByStore(Long storeId, LocalDate date) {
        if (date != null) {
            return salesPersonLoginRepository.findLoginsByStoreAndDate(storeId, date);
        }
        return salesPersonLoginRepository.findByUserIdAndLogoutTimeIsNull(storeId);
    }

    // ============ Associate Authentication for POS Kiosk Mode ============

    /**
     * Authenticate an associate for POS Kiosk sign-in
     */
    @Transactional
    public AssociateAuthResult authenticateAssociate(Long storeId, String associateNumber, String passcode) {
        log.info("Authenticating associate: {} for store: {}", associateNumber, storeId);

        // Try to find by store-specific credential first, then any active credential
        Optional<AssociateCredential> credentialOpt = associateCredentialRepository
                .findByAssociateNumberAndStoreIdAndIsActiveTrue(associateNumber, storeId);

        if (credentialOpt.isEmpty()) {
            credentialOpt = associateCredentialRepository.findByAssociateNumberAndIsActiveTrue(associateNumber);
        }

        AssociateCredential credential = credentialOpt
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // Verify passcode
        if (!passwordEncoder.matches(passcode, credential.getPasscodeHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Get user details
        User user = userRepository.findById(credential.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if shift exists or create one
        Optional<Shift> existingShift = shiftRepository.findActiveShiftByStoreAndUser(storeId, credential.getUserId());

        Shift shift;
        if (existingShift.isPresent()) {
            shift = existingShift.get();
        } else {
            shift = startShift(credential.getUserId(), storeId, null, null, null, "POS Kiosk sign-in");
        }

        log.info("Associate authenticated successfully: {}", associateNumber);

        return new AssociateAuthResult(
                credential.getUserId(),
                associateNumber,
                user.getFirstName() + " " + user.getLastName(),
                shift.getId());
    }

    /**
     * Verify associate passcode for sign-out
     */
    public boolean verifyAssociatePasscode(Long userId, String passcode) {
        log.info("Verifying passcode for user: {}", userId);

        AssociateCredential credential = associateCredentialRepository
                .findByUserIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("Associate not found"));

        return passwordEncoder.matches(passcode, credential.getPasscodeHash());
    }

    // ============ Credential CRUD Operations ============

    /**
     * Get all associate credentials
     */
    public List<CredentialResponse> getAllCredentials() {
        log.info("Getting all associate credentials");

        List<AssociateCredential> credentials = associateCredentialRepository.findAll();

        return credentials.stream().map(c -> {
            String userName = "Unknown";
            String storeName = null;

            try {
                User user = userRepository.findById(c.getUserId()).orElse(null);
                if (user != null) {
                    userName = (user.getFirstName() != null ? user.getFirstName() : "") +
                            " " + (user.getLastName() != null ? user.getLastName() : "");
                    userName = userName.trim();
                    if (userName.isEmpty())
                        userName = user.getUsername();
                }
            } catch (Exception e) {
                log.warn("Could not find user for credential: {}", c.getId());
            }

            if (c.getStoreId() != null) {
                try {
                    storeName = storeRepository.findById(c.getStoreId())
                            .map(s -> s.getName())
                            .orElse(null);
                } catch (Exception e) {
                    log.warn("Could not find store for credential: {}", c.getId());
                }
            }

            return new CredentialResponse(
                    c.getId(),
                    c.getUserId(),
                    userName,
                    c.getAssociateNumber(),
                    c.getStoreId(),
                    storeName,
                    c.getIsActive(),
                    c.getCreatedAt() != null ? c.getCreatedAt().toString() : null);
        }).toList();
    }

    /**
     * Create a new associate credential
     */
    @Transactional
    public CredentialResponse createCredential(Long userId, String associateNumber, String passcode, Long storeId) {
        log.info("Creating credential for user: {} with associate number: {}", userId, associateNumber);

        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Check if associate number already exists
        if (associateCredentialRepository.existsByAssociateNumber(associateNumber)) {
            throw new RuntimeException("Associate number already exists: " + associateNumber);
        }

        // Hash the passcode
        String passcodeHash = passwordEncoder.encode(passcode);

        // Create credential
        AssociateCredential credential = AssociateCredential.builder()
                .userId(userId)
                .associateNumber(associateNumber)
                .passcodeHash(passcodeHash)
                .storeId(storeId)
                .isActive(true)
                .build();

        AssociateCredential saved = associateCredentialRepository.save(credential);
        log.info("Credential created successfully: {}", saved.getId());

        String userName = (user.getFirstName() != null ? user.getFirstName() : "") +
                " " + (user.getLastName() != null ? user.getLastName() : "");
        userName = userName.trim();
        if (userName.isEmpty())
            userName = user.getUsername();

        String storeName = null;
        if (storeId != null) {
            storeName = storeRepository.findById(storeId)
                    .map(s -> s.getName())
                    .orElse(null);
        }

        return new CredentialResponse(
                saved.getId(),
                saved.getUserId(),
                userName,
                saved.getAssociateNumber(),
                saved.getStoreId(),
                storeName,
                saved.getIsActive(),
                saved.getCreatedAt() != null ? saved.getCreatedAt().toString() : null);
    }

    /**
     * Result object for associate authentication
     */
    public record AssociateAuthResult(Long userId, String associateNumber, String name, Long shiftId) {
    }

    /**
     * Response object for credential operations
     */
    public record CredentialResponse(
            Long id,
            Long userId,
            String userName,
            String associateNumber,
            Long storeId,
            String storeName,
            Boolean isActive,
            String createdAt) {
    }
}
