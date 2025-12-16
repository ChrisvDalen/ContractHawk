package com.contracthawk.specification;

import com.contracthawk.entity.ApiContract;
import com.contracthawk.entity.Lifecycle;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ApiContractSpecification {
    
    public static Specification<ApiContract> search(String q, Lifecycle lifecycle, String ownerTeam) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (q != null && !q.trim().isEmpty()) {
                String searchPattern = "%" + q.toLowerCase() + "%";
                Predicate namePred = cb.like(cb.lower(root.get("name")), searchPattern);
                Predicate baseUrlPred = cb.like(cb.lower(root.get("baseUrl")), searchPattern);
                Predicate ownerTeamPred = cb.like(cb.lower(root.get("ownerTeam")), searchPattern);
                Predicate versionPred = cb.like(cb.lower(root.get("version")), searchPattern);
                Predicate descPred = cb.like(cb.lower(root.get("description")), searchPattern);
                
                predicates.add(cb.or(namePred, baseUrlPred, ownerTeamPred, versionPred, descPred));
            }
            
            if (lifecycle != null) {
                predicates.add(cb.equal(root.get("lifecycle"), lifecycle));
            }
            
            if (ownerTeam != null && !ownerTeam.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("ownerTeam")), ownerTeam.toLowerCase()));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

