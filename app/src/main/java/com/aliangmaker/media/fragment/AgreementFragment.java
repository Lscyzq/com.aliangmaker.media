package com.aliangmaker.media.fragment;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aliangmaker.media.databinding.FragmentAgreementBinding;
import com.aliangmaker.media.event.Bean0;

import org.greenrobot.eventbus.EventBus;

public class AgreementFragment extends Fragment {
    private FragmentAgreementBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (binding == null )binding = FragmentAgreementBinding.inflate(getLayoutInflater());
        binding.agTvMain.setText(Html.fromHtml("个人开发者凉生初霜，以及其个人组织的“阿凉创客”产品，始终以用户的隐私作为根本，积极保护用户的信息安全。<br/><br/><font color='#99CC00'>一、隐私协议</font><br/>凉腕播放器所请求的权限如下：<br/>1.获取完全的本地文件访问权限：以正常读取、播放、删除和重命名您本设备上的媒体文件。<br/>2.完整的网络访问权限：以正常播放网络媒体文件（弹幕文件）、上传您的设备信息、播放频次、检测更新和读取公告。<br/>3.保持屏幕常亮：以正常在视频播放界面进行播放。<br/>4.安装来源于此的应用：以正常进行应用内更新。<br/>5.获取您的设备型号：以便于开发者更好地对设备进行适配。<br/><br/><font color='#99CC00'>二、用户使用约定</font><br/>凉腕播放器为完全开源APP（详见github相关项目）在使用时，请您遵循以下条款：<br/>1.不进行违法犯罪活动：包括但不限于播放或下载有损国家荣誉、色情、血腥、暴力视频，情节严重的，移至司法机关追究法律责任。<br/>2.严禁发布破解版软件：包括但不限于未经同意，修改（部分）本APP代码并进行发布传播，如有特殊需求，请与开发者联系。<br/>3.严禁盗取本APP内信息并进行恶意活动：包括但不限于获取服务器地址进行“刷流量”、不正当访问等，开发者有权维权。<br/><br/><font color='#99CC00'>三、免责声明</font><br/>您从事的各种恶意行为，包括但不限于播放不利于社会发展、破解、盗刷资源等非法活动，均与本APP开发者无关，责任由进行非法活动者自负，开发者保留其对APP的所有权益，包括但不限于解释、修改、发布。<br/>"));
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().post(new Bean0(3));
    }

    @Override
    public void onPause() {
        super.onPause();
        TitleFragment.setTitle("使用协议");
    }
}