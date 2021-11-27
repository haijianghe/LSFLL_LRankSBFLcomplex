/**
 * 
 */
package common;

/**
 * @author Administrator
 *该类只是一个操作手册，并无实际功能。
 */
public class OperationManual {
	/*
	 * 1,MainProcessLRankSBFL 是主程序,计算各数据集的性能。
	 *     comment类型为"solo"的要求：
	 *     		I， 根目录有文件object.fault 和 object.testcase
	 *          II, 子目录profile下有各个版本的所有可执行语句的程序谱。
	 *          III,子目录versions.alt下有各个版本的源代码
	 *     comment类型为"multi"的要求：
	 * 2,
	 * 3,
	 *  %%%%%%%%%
	 *  注意：
	 *    comment类型为"solo"的，处理的profile只包含单个源代码文件；
	 *    comment类型为"multi"的profile包含多个源代码文件；
	 *  %%%%%%%%%     
	 */
	/*
	 * Time V5里面有这样的情况， FixedMillisProvider是DateTimeUtils的静态类，还要处理这种情况。
org.joda.time.DateTimeUtils$FixedMillisProvider  547       0.0455802844097073
org.joda.time.DateTimeUtils$FixedMillisProvider  548       0.0455802844097073
org.joda.time.DateTimeUtils$FixedMillisProvider  549       0.0455802844097073
org.joda.time.DateTimeUtils$FixedMillisProvider  556       0.0
org.joda.time.DateTimeUtils$OffsetMillisProvider  571       0.0
org.joda.time.DateTimeUtils$OffsetMillisProvider  572       0.0
org.joda.time.DateTimeUtils$OffsetMillisProvider  573       0.0
org.joda.time.DateTimeUtils$OffsetMillisProvider  580       0.0
	 */
}
