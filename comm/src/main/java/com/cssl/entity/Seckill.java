package com.cssl.entity;

    import com.baomidou.mybatisplus.annotation.IdType;
    import com.baomidou.mybatisplus.extension.activerecord.Model;
    import com.baomidou.mybatisplus.annotation.TableId;
    import java.time.LocalDateTime;
    import java.io.Serializable;
    import lombok.Data;
    import lombok.EqualsAndHashCode;
    import lombok.experimental.Accessors;

/**
* <p>
    * 
    * </p>
*
* @author lx
* @since 2019-09-10
*/
    @Data
        @EqualsAndHashCode(callSuper = false)
    @Accessors(chain = true)
    public class Seckill extends Model<Seckill> {

    private static final long serialVersionUID = 1L;

            @TableId(value = "seckill_id", type = IdType.AUTO)
    private Integer seckill_id;

    private Integer goods_id;

    private Integer seckil_number;

    private LocalDateTime start_time;

    private LocalDateTime end_time;

    private LocalDateTime create_time;

    private Integer standby1;

    private String standby2;


    @Override
    protected Serializable pkVal() {
        return this.seckill_id;
    }

}
