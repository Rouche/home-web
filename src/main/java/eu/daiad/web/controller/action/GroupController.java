package eu.daiad.web.controller.action;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.group.EnumGroupType;
import eu.daiad.web.model.group.Group;
import eu.daiad.web.model.group.GroupInfoResponse;
import eu.daiad.web.model.group.GroupListInfoResponse;
import eu.daiad.web.model.group.GroupMemberInfo;
import eu.daiad.web.model.group.GroupMemberInfoResponse;
import eu.daiad.web.model.group.GroupQueryRequest;
import eu.daiad.web.model.group.GroupQueryResponse;
import eu.daiad.web.model.group.GroupSetCreateRequest;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.RoleConstant;
import eu.daiad.web.repository.application.IFavouriteRepository;
import eu.daiad.web.repository.application.IGroupRepository;

/**
 * Provides actions for managing user defined groups.
  */
@RestController
public class GroupController extends BaseController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(GroupController.class);

    /**
     * Repository for accessing group data.
     */
    @Autowired
    private IGroupRepository groupRepository;

    /**
     * Repository for accessing favourite data.
     */
    @Autowired
    private IFavouriteRepository favouriteRepository;

    /**
     * Returns all groups filtered by a query
     *
     * @param user the authenticated user
     * @param request the query
     * @return the result of the operation
     */
    @RequestMapping(value = "/action/group", method = RequestMethod.POST, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getGroups(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody GroupQueryRequest request) {
        try {
            GroupQueryResponse response = new GroupQueryResponse();

            response.setGroups(groupRepository.getGroupsByUtilityKey(user.getUtilityKey()));

            for (Group g : response.getGroups()) {
                if (g.getType() == EnumGroupType.SET) {
                    g.setFavorite(favouriteRepository.isGroupFavorite(user.getKey(), g.getKey()));
                }
            }
            return response;
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Returns all groups filtered by name
     *
     * @param user the authenticated user
     * @param text the text to search for
     * @return the selected groups
     */
    @RequestMapping(value = "/action/group/query/{text}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getGroups(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String text) {
        try {
            GroupQueryResponse response = new GroupQueryResponse();

            response.setGroups(groupRepository.filterByName(user.getUtilityKey(), text));

            for (Group g : response.getGroups()) {
                if (g.getType() == EnumGroupType.SET) {
                    g.setFavorite(favouriteRepository.isGroupFavorite(user.getKey(), g.getKey()));
                }
            }
            return response;
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Enumerates user defined groups.
     *
     * @return the available groups.
     */
    @RequestMapping(value = "/action/groups", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getGroupsInfo(@AuthenticationPrincipal AuthenticatedUser user) {
        try {
            return new GroupListInfoResponse(groupRepository.getUtilityGroupInfo(user.getUtilityKey()));
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Creates a new group
     *
     * @param user the authenticated user
     * @param request the group creation request
     * @return the result of the operation
     */
    @RequestMapping(value = "/action/group", method = RequestMethod.PUT, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public @ResponseBody RestResponse create(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody GroupSetCreateRequest request) {
        RestResponse response = new RestResponse();
        try {
            if (StringUtils.isBlank(request.getTitle())) {
                return new RestResponse(SharedErrorCode.UNKNOWN.getMessageKey(), "No title selected");
            }
            if ((request.getMembers() == null) || (request.getMembers().length == 0)) {
                return new RestResponse(SharedErrorCode.UNKNOWN.getMessageKey(), "No members selected");
            }
            groupRepository.createGroupSet(user.getKey(), request.getTitle(), request.getMembers());
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Get a group by its id.
     *
     * @param key the group key.
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/group/{key}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getGroupInfoByKey(@PathVariable UUID key) {
        try{
            return new GroupInfoResponse(groupRepository.getGroupInfoByKey(key));
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Gets the members of a group.
     *
     * @param key the group key.
     * @return a collection of {@link GroupMemberInfo}.
     */
    @RequestMapping(value = "/action/group/members/{key}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getGroupMembers(@PathVariable UUID key) {
        try{
            return new GroupMemberInfoResponse(groupRepository.getGroupMemberInfo(key));
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }


    /**
     * Deletes a group
     *
     * @param user the authenticated user
     * @param groupKey the key of the group
     * @return the result of the operation
     */
    @RequestMapping(value = "/action/group/{groupKey}", method = RequestMethod.DELETE, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public @ResponseBody RestResponse remove(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID groupKey) {
        RestResponse response = new RestResponse();

        try {
            groupRepository.deleteGroupSet(groupKey);
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }
        return response;
    }

    /**
     * Adds a group to the favorite list
     *
     * @param groupKey the key of the group to add
     * @return the result of the operation
     */
    @RequestMapping(value = "/action/group/favorite/{groupKey}", method = RequestMethod.PUT, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public @ResponseBody RestResponse addFavorite(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID groupKey) {
        RestResponse response = new RestResponse();
        try {
            favouriteRepository.addGroupFavorite(user.getKey(), groupKey);

            return new RestResponse();
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }
        return response;
    }

    /**
     * Removes a group from the favorite list
     *
     * @param groupKey the key of the group to remove
     * @return the result of the operation
     */
    @RequestMapping(value = "/action/group/favorite/{groupKey}", method = RequestMethod.DELETE, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public @ResponseBody RestResponse removeFavorite(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID groupKey) {
        RestResponse response = new RestResponse();
        try {
            favouriteRepository.deleteGroupFavorite(user.getKey(), groupKey);
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }
        return response;
    }

}
