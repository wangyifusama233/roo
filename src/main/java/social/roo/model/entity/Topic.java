package social.roo.model.entity;

import com.blade.jdbc.annotation.Table;
import com.blade.jdbc.core.ActiveRecord;
import com.blade.validator.annotation.NotEmpty;
import lombok.Data;

import java.util.Date;

/**
 * 主题
 *
 * @author biezhi
 * @date 2017/8/1
 */
@Table(value = "roo_topic", pk = "tid")
@Data
public class Topic extends ActiveRecord {

    private String  tid;
    @NotEmpty(message = "请选择节点")
    private String  nodeSlug;
    private String  nodeTitle;
    @NotEmpty(message = "请请输入标题")
    private String  title;
    @NotEmpty(message = "请请输入内容")
    private String  content;
    private String  username;
    private Integer comments;
    private Integer gains;
    private Double  weight;
    private Boolean popular;
    private String  replyUser;
    private Date    created;
    private Date    updated;
    private Date    replyed;

}