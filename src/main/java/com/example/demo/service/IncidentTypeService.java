package com.example.demo.service;

import com.example.demo.model.IncidentType;
import com.example.demo.model.Role;
import com.example.demo.repository.IncidentTypeRepository;
import jakarta.persistence.Column;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class IncidentTypeService {

    private final IncidentTypeRepository incidentTypeRepository;
@Autowired
    public IncidentTypeService(IncidentTypeRepository incidentTypeRepository) {
        this.incidentTypeRepository = incidentTypeRepository;
    }

    // ✅ اضافه کردن نوع حادثه (همه نقش‌ها مجازند)
    public IncidentType createIncidentType(IncidentType incidentType, Role role) {
        if (role == Role.USER || role == Role.ADMIN || role == Role.RESPONSE) {
            return incidentTypeRepository.save(incidentType);
        }
        throw new RuntimeException("عدم دسترسی");
    }

    // 📋 مشاهده همه نوع‌های حادثه (فقط ADMIN و RESPONSE)
    public List<IncidentType> getAllIncidentTypes(Role role) {
        if (role == Role.ADMIN || role == Role.RESPONSE) {
            return incidentTypeRepository.findAll();
        }
        throw new RuntimeException("عدم دسترسی برای مشاهده لیست");
    }

    // 🔍 جستجو بر اساس آیدی (بدون محدودیت نقش)
    public Optional<IncidentType> getIncidentTypeById(Long id) {
        return incidentTypeRepository.findById(id);
    }

    // 🔍 جستجو بر اساس کد (بدون محدودیت نقش)
    public Optional<IncidentType> getByCode(String code) {
        return incidentTypeRepository.findByCode(code);
    }

    // ❌ حذف نوع حادثه (فقط ADMIN)
    public void deleteIncidentType(Long id, Role role) {
        if (role != Role.ADMIN) {
            throw new RuntimeException("فقط مدیر می‌تواند حذف کند");
        }
        incidentTypeRepository.deleteById(id);
    }

    // ✏️ ویرایش نوع حادثه (فقط ADMIN و RESPONSE)
    public IncidentType updateIncidentType(Long id, IncidentType updatedIncidentType, Role role) {
        if (role != Role.ADMIN && role != Role.RESPONSE) {
            throw new RuntimeException("فقط ادمین یا امدادگر می‌تواند ویرایش کند");
        }

        return incidentTypeRepository.findById(id)
                .map(existing -> {
                    existing.setCode(updatedIncidentType.getCode());
                    existing.setTitle(updatedIncidentType.getTitle());
                    return incidentTypeRepository.save(existing);
                }).orElseThrow(() -> new RuntimeException("نوع حادثه یافت نشد"));
    }
    public IncidentType uploadIcon(Long id, MultipartFile file) throws IOException {
        IncidentType type = incidentTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("نوع حادثه پیدا نشد"));

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String uploadDir = "uploads/icons/";

        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        Path path = Paths.get(uploadDir + fileName);
        Files.write(path, file.getBytes());

        type.setIconPath(path.toString());
        return incidentTypeRepository.save(type);
    }

}

